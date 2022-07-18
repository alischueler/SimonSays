import tester.*;                // The tester library
import java.util.Random;
import javalib.worldimages.*;   // images, like RectangleImage or OverlayImages
import javalib.funworld.*;      // the abstract World class and the big-bang library
import java.awt.Color;          // general colors (as triples of red,green,blue values)
// and predefined colors (Red, Green, Yellow, Blue, Black, White)

class Circles extends World { //represents the world
  ILoCircle circlesDrawn;
  ILoCircle circlesLeft; //represents the circles in the list to be clicked or blinked
  ILoCircle circlesUsed; //represents the circles in the list that have been clicked or blinked
  boolean isFlashing; //represents if the world should be blinking or not
  boolean isDarkened; //represents if it has been darkened or not
  boolean addCircle; //represents if circles should be added to the list or not
  boolean acceptClicks; //represents if the game is accepting mouse clicks or not
  boolean endWorld; //represents if the game should be stopped or not
  WorldScene lastImage = 
      new WorldScene(400, 600).placeImageXY(new TextImage("Wrong Circle", Color.black), 
          200, 300); //represents the last image that should be shown when the game ends

  Circles(ILoCircle circlesDrawn, ILoCircle circlesLeft, ILoCircle circlesUsed, 
      boolean isFlashing, boolean isDarkened, boolean addCircle, boolean acceptClicks) {
    this.circlesDrawn = circlesDrawn;
    this.circlesLeft = circlesLeft;
    this.circlesUsed = circlesUsed;
    this.isFlashing = isFlashing;
    this.isDarkened = isDarkened;
    this.addCircle = addCircle;
    this.acceptClicks = acceptClicks;
  }

  Circles(boolean endWorld) {
    this.endWorld = endWorld;
  }

  //draws the colors onto the background
  public WorldScene makeScene() {
    if (!this.endWorld) {
      return this.circlesDrawn.draw(new WorldScene(400, 600));
    }
    else {
      return this.lastImage;
    }
  }

  //darken's the circles in the list at every tick
  public Circles onTick() {
    if (this.isFlashing && !this.isDarkened) {
      return this.circlesLeft.blinkDarken(this.circlesUsed, this.circlesDrawn);
    }
    else if (this.isFlashing && this.isDarkened) {
      return this.circlesLeft.blinkLighten(this.circlesUsed, this.circlesDrawn);
    }
    else {
      return this;
    }
  }

  //adds one circle to this world
  public Circles addCircles() {
    ILoCircle newCircles = this.circlesLeft.append(new Circle(new Random(), 70, 70));
    if (this.addCircle) {
      return new Circles(this.circlesDrawn, newCircles, new MtLoCircle(), 
          true, false, false, false);
    }
    else {
      return this;
    }
  }

  //adds one Circle to this world
  public Circles addCirclesForTesting() {
    ILoCircle newCirclesTest = this.circlesLeft.append(new Circle(new Random(4)));
    if (this.addCircle) {
      return new Circles(this.circlesDrawn, newCirclesTest, new MtLoCircle(),
          true, false, false, false);
    }
    else {
      return this;
    }
  }

  //determines the position of the mouse click
  public Circles onMouseClicked(Posn pos) {
    if (this.acceptClicks) {
      return this.circlesLeft.onMouseClickedHelp(pos, this.circlesUsed, this.circlesDrawn);
    }
    else {
      return this;
    }
  }

  //determines the position of the mouse click
  public Circles onMouseClickedForTesting(Posn pos) {
    if (this.acceptClicks) {
      return this.circlesLeft.onMouseClickedHelpForTesting(pos, this.circlesUsed, 
          this.circlesDrawn);
    }
    else {
      return this;
    }
  }

}

class Circle { //represents a circle
  int radius = 30;
  Color c;
  int x;
  int y;

  Circle(Color c, int x, int y) {
    this.c = c;
    this.x = x;
    this.y = y;
  }

  Circle(Random r, int x, int y) {
    if (r.nextInt(4) == 1) {
      this.c = Color.red;
      this.x = x;
      this.y = y;
    }

    else {
      if (r.nextInt(4) == 2) {
        this.c = Color.yellow;
        this.x = x + 80;
        this.y = y;
      }

      else {
        if (r.nextInt(4) == 3) {
          this.c = Color.blue;
          this.x = x;
          this.y = y + 80;
        }

        else {
          this.c = Color.GREEN;
          this.x = x + 80;
          this.y = y + 80;
        }
      }
    }
  }

  Circle(Random r) {
    this(Color.green, 150, 150);
  }

  //draw this Circle onto the scene
  WorldScene draw(WorldScene acc) {
    return acc.placeImageXY(new CircleImage(this.radius, "solid", this.c),  this.x, this.y);
  }

  //brightens the color of the given circle
  ILoCircle brightenCircle(ILoCircle drawn) {
    return new ConsLoCircle(new Circle(drawn.findCircle(this).c.brighter().brighter(), 
        this.x, this.y), drawn.remove(this));
  }

  //brightens the color of the given circle
  ILoCircle darkenCircle(ILoCircle drawn) {
    return new ConsLoCircle(new Circle(drawn.findCircle(this).c.darker().darker(), 
        this.x, this.y), drawn.remove(this));
  }

  //finds the color of this circle
  boolean findColor(Circle c) {
    return this.x == c.x && this.y == c.y;
  }

  //is the x mouse location in the area of this circle?
  public boolean isInsideX(int x) {
    return ((this.x - this.radius) <= x) && ((this.x + this.radius) >= x);
  }

  //is the y mouse location in the area of this circle?
  public boolean isInsideY(int y) {
    return ((this.y - this.radius) <= y) && ((this.y + this.radius) >= y);
  }

}

interface ILoCircle { //represents a list of circles

  //draw the colors on the given scene
  WorldScene draw(WorldScene acc);

  //blink the colors in the given order
  Circles blinkDarken(ILoCircle blinked, ILoCircle drawn);

  //blink the colors in the given order
  Circles blinkLighten(ILoCircle blinked, ILoCircle drawn);

  //determines if the player clicked the correct positions for this ILoCircle
  Circles onMouseClickedHelp(Posn pos, ILoCircle clicked, ILoCircle drawn);

  //determines if the player clicked the correct positions for this ILoCircle
  Circles onMouseClickedHelpForTesting(Posn pos, ILoCircle clicked, ILoCircle drawn);

  //finds the given circle in this list of Circles
  Circle findCircle(Circle c);

  //removes the given circle from this ConsLoCircle
  ILoCircle remove(Circle c);

  //appends this ILoCircle and the given Circle
  ILoCircle append(Circle c);

}

class MtLoCircle implements ILoCircle {
  MtLoCircle() {}

  //draw the colors on the given scene
  public WorldScene draw(WorldScene acc) {
    return acc;
  }

  //determines if the mouse click inside this empty list of circles
  public Circles onMouseClickedHelp(Posn pos, ILoCircle clicked, ILoCircle drawn) {
    return new Circles(drawn, clicked, new MtLoCircle(), false, false, true, false).addCircles();
  }

  //determines if the mouse click inside this empty list of circles
  public Circles onMouseClickedHelpForTesting(Posn pos, ILoCircle clicked, ILoCircle drawn) {
    return new Circles(drawn, clicked, new MtLoCircle(), 
        false, false, true, false).addCirclesForTesting();
  }

  //darken's this empty list of circles
  public Circles blinkDarken(ILoCircle blinked, ILoCircle drawn) {
    return new Circles(drawn, this, blinked, true, true, false, false);
  }

  //brightens this empty list of circles
  public Circles blinkLighten(ILoCircle blinked, ILoCircle drawn) {
    return new Circles(drawn, blinked, new MtLoCircle(), false, false, false, true);
  }

  //finds the given circle in this empty list of circles
  public Circle findCircle(Circle c) {
    return c;
  }

  //removes the given circle from this Empty list of circles
  public ILoCircle remove(Circle c) {
    return this;
  }

  //appends the given circle to this empty list of circles
  public ILoCircle append(Circle c) {
    return new ConsLoCircle(c, new MtLoCircle());
  }
}

class ConsLoCircle implements ILoCircle {
  Circle first;
  ILoCircle rest;

  ConsLoCircle(Circle first, ILoCircle rest) {
    this.first = first;
    this.rest = rest;
  }

  //draw the colors on the given scene
  public WorldScene draw(WorldScene acc) {
    return this.rest.draw(this.first.draw(acc));
  }

  //lightens the colors in the given order
  public Circles blinkDarken(ILoCircle blinked, ILoCircle drawn) {
    return new Circles(this.first.darkenCircle(drawn), this, blinked,
        true, true, false, false);
  }

  //darken's the colors in the given order
  public Circles blinkLighten(ILoCircle blinked, ILoCircle drawn) {
    return new Circles(this.first.brightenCircle(drawn), this.rest, blinked.append(this.first),
        true, false, false, false);
  }

  //is the mouse click inside this list of circles?
  public Circles onMouseClickedHelp(Posn pos, ILoCircle clicked, ILoCircle drawn) {
    if (this.first.isInsideX(pos.x) && this.first.isInsideY(pos.y)) {
      return new Circles(drawn, this.rest, clicked.append(this.first), 
          false, false, false, true);
    }
    else {
      return new Circles(true);
    }
  }

  //is the mouse click inside this list of circles?
  public Circles onMouseClickedHelpForTesting(Posn pos, ILoCircle clicked, ILoCircle drawn) {
    if (this.first.isInsideX(pos.x) && this.first.isInsideY(pos.y)) {
      return new Circles(drawn, this.rest, clicked.append(this.first), 
          false, false, false, true);
    }
    else {
      return new Circles(true);
    }
  }

  //finds the given circle in this list of drawn circles
  public Circle findCircle(Circle c) {
    if (this.first.findColor(c)) {
      return this.first;
    }
    else {
      return this.rest.findCircle(c);
    }
  }

  //removes the given circle from this ConsLoCircle
  public ILoCircle remove(Circle c) {
    if (this.first.findColor(c)) {
      return this.rest;
    }
    else {
      return new ConsLoCircle(this.first, this.rest.remove(c));
    }
  }

  //appends the given circle to this ConsLoCircle
  public ILoCircle append(Circle c) {
    return new ConsLoCircle(this.first, this.rest.append(c));
  }
}

class ExamplesCircles {
  Circle c1 = new Circle(Color.red, 70, 70);
  Circle c2 = new Circle(Color.YELLOW, 150, 70);
  Circle c3 = new Circle(Color.BLUE, 70, 150);
  Circle c4 = new Circle(Color.GREEN, 150, 150);
  Circle c0 = new Circle(new Random());
  ILoCircle mt = new MtLoCircle();
  ILoCircle loc0 = new ConsLoCircle(this.c0, this.mt);
  ILoCircle loc1 = new ConsLoCircle(this.c1, this.mt);
  ILoCircle loc2 = new ConsLoCircle(this.c2, this.loc1);
  ILoCircle loc3 = new ConsLoCircle(this.c3, this.loc2);
  ILoCircle loc4 = new ConsLoCircle(this.c4, this.loc3);
  Circles world0 = new Circles(this.loc4, this.loc0, this.mt, true, false, false, false);
  Circles world1 = new Circles(this.loc4, this.loc1, this.mt, true, false, false, false);
  Circles world2 = new Circles(this.loc4, this.loc1, this.mt, false, false, true, false);
  Circles world3 = new Circles(this.loc4, this.loc1, this.loc1, true, true, false, false);
  Circles world3A = new Circles(this.loc4, this.loc1, this.loc1, false, false, false, true);
  Circles world3B = new Circles(this.loc4, this.loc1, this.loc1, false, false, true, false);
  Circles world4 = new Circles(this.loc4, this.loc3, this.mt, true, false, false, false);
  Circles world4A = new Circles(this.loc4, this.loc3, this.mt, false, false, false, true);
  Circles world4B = new Circles(this.loc4, this.loc3, this.mt, false, false, true, false);

  boolean testMakeScene(Tester t) {
    return t.checkExpect(this.world1.makeScene(), (new WorldScene(400, 600)
        .placeImageXY(new CircleImage(30, "solid", Color.green), 150, 150))
        .placeImageXY(new CircleImage(30, "solid", Color.blue), 70, 150)
        .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70)
        .placeImageXY(new CircleImage(30, "solid", Color.red), 70, 70))
        && t.checkExpect(this.world2.makeScene(), (new WorldScene(400, 600)
            .placeImageXY(new CircleImage(30, "solid", Color.green), 150, 150))
            .placeImageXY(new CircleImage(30, "solid", Color.blue), 70, 150)
            .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70)
            .placeImageXY(new CircleImage(30, "solid", Color.red), 70, 70))
        && t.checkExpect(this.world3.makeScene(), (new WorldScene(400, 600)
            .placeImageXY(new CircleImage(30, "solid", Color.green), 150, 150))
            .placeImageXY(new CircleImage(30, "solid", Color.blue), 70, 150)
            .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70)
            .placeImageXY(new CircleImage(30, "solid", Color.red), 70, 70));
  }

  boolean testDraw(Tester t) {
    return t.checkExpect(this.c1.draw(new WorldScene(400, 600)), 
        new WorldScene(400, 600).placeImageXY(
            new CircleImage(30, "solid", Color.red), 70, 70))
        && t.checkExpect(this.c2.draw(new WorldScene(400, 600)
            .placeImageXY(new CircleImage(30, "solid", Color.red), 70, 70)), 
            new WorldScene(400, 600).placeImageXY(
                new CircleImage(30, "solid", Color.red), 70, 70)
            .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70))
        && t.checkExpect(this.c3.draw(new WorldScene(400, 600).placeImageXY(
            new CircleImage(30, "solid", Color.red), 70, 70)
            .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70)),
            (new WorldScene(400, 600).placeImageXY(new CircleImage(30, "solid", Color.red), 
                70, 70))
            .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70)
            .placeImageXY(new CircleImage(30, "solid", Color.BLUE), 70, 150))
        && t.checkExpect(this.mt.draw(new WorldScene(400, 600)), new WorldScene(400, 600))
        && t.checkExpect(this.mt.draw(new WorldScene(400, 600).placeImageXY(
            new CircleImage(30, "solid", Color.red), 70, 70)), 
            new WorldScene(400, 600).placeImageXY(
                new CircleImage(30, "solid", Color.red), 70, 70))
        && t.checkExpect(this.loc1.draw(new WorldScene(400, 600)), new WorldScene(400, 600)
            .placeImageXY(new CircleImage(30, "solid", Color.red), 70, 70))
        && t.checkExpect(this.loc4.draw(new WorldScene(400, 600)), (new WorldScene(400, 600)
            .placeImageXY(new CircleImage(30, "solid", Color.green), 150, 150))
            .placeImageXY(new CircleImage(30, "solid", Color.blue), 70, 150)
            .placeImageXY(new CircleImage(30, "solid", Color.yellow), 150, 70)
            .placeImageXY(new CircleImage(30, "solid", Color.red), 70, 70));
  }

  boolean testOnTick(Tester t) {
    return t.checkExpect(this.world1.onTick(), new Circles(new ConsLoCircle(
        new Circle(Color.red.darker().darker(), 70, 70),
        new ConsLoCircle(this.c4, 
            new ConsLoCircle(this.c3, 
                new ConsLoCircle(this.c2, this.mt)))), this.loc1, this.mt, 
        true, true, false, false))
        && t.checkExpect(this.world2.onTick(), this.world2)
        && t.checkExpect(this.world3.onTick(), new Circles(
            new ConsLoCircle(new Circle(Color.red, 70, 70),
                new ConsLoCircle(this.c4, 
                    new ConsLoCircle(this.c3, 
                        new ConsLoCircle(this.c2, this.mt)))), 
            this.mt, new ConsLoCircle(this.c1, new ConsLoCircle(this.c1, this.mt)), 
            true, false, false, false))
        && t.checkExpect(this.world4.onTick(), 
            new Circles(new ConsLoCircle(new Circle(Color.blue.darker().darker(), 70, 150),
                new ConsLoCircle(this.c4,
                    new ConsLoCircle(this.c2, 
                        new ConsLoCircle(this.c1, this.mt)))), 
                this.loc3, this.mt, true, true, false, false));
  }

  boolean testAddCirclesForTesting(Tester t) {
    return t.checkExpect(this.world1.addCirclesForTesting(), this.world1)
        && t.checkExpect(this.world2.addCirclesForTesting(), 
            new Circles(this.loc4, new ConsLoCircle(this.c1, 
                new ConsLoCircle(this.c4, this.mt)), this.mt, true, false, false, false))
        && t.checkExpect(this.world3.addCirclesForTesting(), this.world3)
        && t.checkExpect(this.world3B.addCirclesForTesting(), 
            new Circles(this.loc4, 
                new ConsLoCircle(this.c1,
                    new ConsLoCircle(this.c4,this.mt)), this.mt, true, false, false, false))
        && t.checkExpect(this.world4.addCirclesForTesting(), this.world4)
        && t.checkExpect(this.world4B.addCirclesForTesting(), 
            new Circles(this.loc4, new ConsLoCircle(this.c3,
                new ConsLoCircle(this.c2,
                    new ConsLoCircle(this.c1,
                        new ConsLoCircle(this.c4, this.mt)))), this.mt, true, false, false, false));
  }

  boolean testOnMouseClickedForTesting(Tester t) {
    return t.checkExpect(this.world1.onMouseClickedForTesting(new Posn(60, 70)), 
        new Circles(this.loc4, this.loc1, this.mt, true, false, false, false))
        && t.checkExpect(this.world2.onMouseClickedForTesting(new Posn(200, 200)), this.world2)
        && t.checkExpect(this.world3A.onMouseClickedForTesting(new Posn(50, 80)), 
            new Circles(this.loc4, this.mt, new ConsLoCircle(this.c1, 
                new ConsLoCircle(this.c1, this.mt)), false, false, false, true))
        && t.checkExpect(this.world4.onMouseClickedForTesting(new Posn(63, 177)), this.world4)
        && t.checkExpect(this.world4A.onMouseClickedForTesting(new Posn(148, 177)), 
            new Circles(true))
        && t.checkExpect(this.world4A.onMouseClickedForTesting(new Posn(48, 159)), 
            new Circles(this.loc4, this.loc2, 
                new ConsLoCircle(this.c3, this.mt), false, false, false, true));
  }

  boolean testBrightenCircle(Tester t) {
    return t.checkExpect(this.c1.brightenCircle(loc4), 
        new ConsLoCircle(new Circle(Color.red, 70, 70),
            new ConsLoCircle(this.c4, new ConsLoCircle(this.c3, 
                new ConsLoCircle(this.c2, this.mt)))))
        && t.checkExpect(this.c2.brightenCircle(loc4), 
            new ConsLoCircle(new Circle(Color.yellow, 150, 70),
                new ConsLoCircle(this.c4, new ConsLoCircle(this.c3, 
                    new ConsLoCircle(this.c1, this.mt)))))
        && t.checkExpect(this.c3.brightenCircle(loc4), 
            new ConsLoCircle(new Circle(Color.blue, 70, 150),
                new ConsLoCircle(this.c4, new ConsLoCircle(this.c2, 
                    new ConsLoCircle(this.c1, this.mt)))))
        && t.checkExpect(this.c4.brightenCircle(loc4), 
            new ConsLoCircle(new Circle(Color.green, 150, 150),
                new ConsLoCircle(this.c3, new ConsLoCircle(this.c2, 
                    new ConsLoCircle(this.c1, this.mt)))));
  }

  boolean testDarkenCircle(Tester t) {
    return t.checkExpect(this.c1.darkenCircle(loc4), 
        new ConsLoCircle(new Circle(Color.red.darker().darker(), 70, 70),
            new ConsLoCircle(this.c4, new ConsLoCircle(this.c3, 
                new ConsLoCircle(this.c2, this.mt)))))
        && t.checkExpect(this.c2.darkenCircle(loc4), 
            new ConsLoCircle(new Circle(Color.yellow.darker().darker(), 150, 70),
                new ConsLoCircle(this.c4, new ConsLoCircle(this.c3, 
                    new ConsLoCircle(this.c1, this.mt)))))
        && t.checkExpect(this.c3.darkenCircle(loc4), 
            new ConsLoCircle(new Circle(Color.blue.darker().darker(), 70, 150),
                new ConsLoCircle(this.c4, new ConsLoCircle(this.c2, 
                    new ConsLoCircle(this.c1, this.mt)))))
        && t.checkExpect(this.c4.darkenCircle(loc4), 
            new ConsLoCircle(new Circle(Color.green.darker().darker(), 150, 150),
                new ConsLoCircle(this.c3, new ConsLoCircle(this.c2, 
                    new ConsLoCircle(this.c1, this.mt)))));
  }

  boolean testBlinkDarken(Tester t) {
    return t.checkExpect(this.loc1.blinkDarken(this.mt, this.loc4), 
        new Circles(new ConsLoCircle(new Circle(Color.red.darker().darker(), 70, 70),
            new ConsLoCircle(this.c4, new ConsLoCircle(this.c3, 
                new ConsLoCircle(this.c2, this.mt)))), 
            this.loc1, this.mt, true, true, false, false))
        && t.checkExpect(this.mt.blinkDarken(this.mt, this.loc4),
            new Circles(this.loc4, this.mt, this.mt, true, true, false, false))
        && t.checkExpect(this.mt.blinkDarken(this.loc1, this.loc4),
            new Circles(this.loc4, this.mt, this.loc1, true, true, false, false))
        && t.checkExpect(this.loc3.blinkDarken(this.loc2, this.loc4),
            new Circles(new ConsLoCircle(new Circle(Color.blue.darker().darker(), 70, 150), 
                new ConsLoCircle(this.c4, 
                    new ConsLoCircle(this.c2,
                        new ConsLoCircle(this.c1, this.mt)))), this.loc3, this.loc2, 
                true, true, false, false));
  }

  boolean testBlinkLighten(Tester t) {
    return t.checkExpect(this.loc1.blinkLighten(this.mt, this.loc4), 
        new Circles(new ConsLoCircle(new Circle(Color.red, 70, 70),
            new ConsLoCircle(this.c4, new ConsLoCircle(this.c3, 
                new ConsLoCircle(this.c2, this.mt)))), 
            this.mt, new ConsLoCircle(this.c1, this.mt), true, false, false, false))
        && t.checkExpect(this.mt.blinkLighten(this.mt, this.loc4), 
            new Circles(this.loc4, this.mt, this.mt, false, false, false, true))
        && t.checkExpect(this.mt.blinkLighten(this.loc2, this.loc4), 
            new Circles(this.loc4, this.loc2, this.mt, false, false, false, true))
        && t.checkExpect(this.loc1.blinkLighten(this.loc3, this.loc4), 
            new Circles(new ConsLoCircle(new Circle(Color.red, 70, 70),
                new ConsLoCircle(this.c4, 
                    new ConsLoCircle(this.c3, 
                        new ConsLoCircle(this.c2, this.mt)))), 
                this.mt, new ConsLoCircle(this.c3, 
                    new ConsLoCircle(this.c2, 
                        new ConsLoCircle(this.c1, 
                            new ConsLoCircle(this.c1, this.mt)))), true, false, false, false));
  }

  boolean testonMouseClickedHelpForTesting(Tester t) {
    return t.checkExpect(this.mt.onMouseClickedHelpForTesting(
        new Posn(50, 50), this.loc1, this.loc4),
        new Circles(this.loc4, 
            new ConsLoCircle(this.c1, 
                new ConsLoCircle(this.c4, this.mt)), this.mt, true, false, false, false))
        && t.checkExpect(this.mt.onMouseClickedHelpForTesting(
            new Posn(200, 200), this.loc4, this.loc4),
            new Circles(this.loc4, 
                new ConsLoCircle(this.c4,
                    new ConsLoCircle(this.c3, 
                        new ConsLoCircle(this.c2,
                            new ConsLoCircle(this.c1, 
                                new ConsLoCircle(this.c4, this.mt))))), 
                this.mt, true, false, false, false))
        && t.checkExpect(this.loc1.onMouseClickedHelpForTesting(
            new Posn(200, 200), this.loc2, this.loc4),
            new Circles(true))
        && t.checkExpect(this.loc1.onMouseClickedHelpForTesting(
            new Posn(60, 147), this.loc4, this.loc4),
            new Circles(true))
        && t.checkExpect(this.loc1.onMouseClickedHelpForTesting(
            new Posn(50, 60), this.loc1, this.loc4), 
            new Circles(this.loc4, this.mt, new ConsLoCircle(this.c1, 
                new ConsLoCircle(this.c1, this.mt)), false, false, false, true));
  }


  boolean testAppend(Tester t) {
    return t.checkExpect(this.mt.append(this.c1), 
        new ConsLoCircle(this.c1, this.mt))
        && t.checkExpect(this.loc1.append(this.c2), 
            new ConsLoCircle(this.c1, new ConsLoCircle(this.c2, this.mt)))
        && t.checkExpect(this.loc3.append(this.c4), 
            new ConsLoCircle(this.c3, 
                new ConsLoCircle(this.c2, 
                    new ConsLoCircle(this.c1, 
                        new ConsLoCircle(this.c4, this.mt)))))
        && t.checkExpect(this.loc2.append(this.c2), 
            new ConsLoCircle(this.c2,
                new ConsLoCircle(this.c1, 
                    new ConsLoCircle(this.c2, this.mt))));
  }

  boolean testRemove(Tester t) {
    return t.checkExpect(this.mt.remove(this.c1), new MtLoCircle())
        && t.checkExpect(this.loc3.remove(this.c2), 
            new ConsLoCircle(this.c3, new ConsLoCircle(this.c1, this.mt)))
        && t.checkExpect(this.loc2.remove(this.c2), 
            new ConsLoCircle(this.c1, this.mt))
        && t.checkExpect(this.loc1.remove(this.c2), this.loc1);
  }

  boolean testisInsideX(Tester t) {
    return t.checkExpect(this.c1.isInsideX(50), true)
        && t.checkExpect(this.c1.isInsideX(200), false)
        && t.checkExpect(this.c2.isInsideX(40), false)
        && t.checkExpect(this.c2.isInsideX(130), true)
        && t.checkExpect(this.c3.isInsideX(60), true)
        && t.checkExpect(this.c3.isInsideX(200), false)
        && t.checkExpect(this.c4.isInsideX(30), false)
        && t.checkExpect(this.c4.isInsideX(160), true);
  }

  boolean testisInsideY(Tester t) {
    return t.checkExpect(this.c1.isInsideY(50), true)
        && t.checkExpect(this.c1.isInsideY(200), false)
        && t.checkExpect(this.c2.isInsideY(70), true)
        && t.checkExpect(this.c2.isInsideY(10), false)
        && t.checkExpect(this.c3.isInsideY(30), false)
        && t.checkExpect(this.c3.isInsideY(175), true)
        && t.checkExpect(this.c4.isInsideY(110), false)
        && t.checkExpect(this.c4.isInsideY(127), true);
  }

  boolean testFindColor(Tester t) {
    return t.checkExpect(this.c1.findColor(this.c1), true)
        && t.checkExpect(this.c3.findColor(this.c2), false)
        && t.checkExpect(this.c4.findColor(this.c3), false)
        && t.checkExpect(this.c2.findColor(this.c2), true);
  }

  boolean testFindCircle(Tester t) {
    return t.checkExpect(this.mt.findCircle(this.c1), this.c1)
        && t.checkExpect(this.loc1.findCircle(c1), this.c1)
        && t.checkExpect(this.loc3.findCircle(c2), this.c2)
        && t.checkExpect(this.loc2.findCircle(c3), this.c3)
        && t.checkExpect(this.loc2.findCircle(c2), this.c2)
        && t.checkExpect(this.loc1.findCircle(c1), this.c1);
  }

  boolean testBigBang(Tester t) {
    Circles world = world0;
    int worldWidth = 400;
    int worldHeight = 600;
    double tickRate = 0.1;
    return world.bigBang(worldWidth, worldHeight, tickRate);
  }
}

//we choose to use one class for both game states because we thought it would be easier to 
//access these fields in the most important methods(world handlers) within the World class 
//itself. We did not understand how it would be possible to relate the two worlds together.