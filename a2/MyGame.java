package a2;
import tage.*;
import tage.shapes.*;
import tage.nodeControllers.*;
import tage.input.*;
import tage.input.action.*;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import javax.swing.*;
import org.joml.*;

import java.util.ArrayList;
import java.util.Random;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import java.util.concurrent.TimeUnit;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private CameraOrbit3D orbitController; 

	private Vector3f currentPosition;
	private double startTime, prevTime, elapsedTime, deltaTime;

	private GameObject cube, plane, childNeptune, neptune, childJupiter, jupiter, childSaturn, saturn, childEarth, earth, avatar, x, y, z;
	private ObjShape cubeS, planeS, sphereS, dolS, linxS, linyS, linzS;
	private TextureImage cubex, earthx, saturnx, jupiterx, neptunex, doltx;
	private Light light;
	private NodeController rcY, rcZ, bc;

	/* New Attributes */
	private float minDistance = 0.50f; // Minimum distance to collect a game object.
	private float maxDistance = 5.0f; // The max distance the camera can be from dolphin.
	private float dolphinLife = 100.0f;
	private int itemCount = 0;
	private int maxItemCnt = 4;

	private boolean earthCollected = false;
	private boolean jupiterCollected = false;
	private boolean saturnCollected = false;
	private boolean neptuneCollected = false;

	public MyGame() { super(); }

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		cubeS = new Cube();
		planeS = new Plane();
		sphereS = new Sphere();
		dolS = new ImportedModel("dolphinHighPoly.obj");
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures()
	{	
		cubex = new TextureImage("starmap.jpg");
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		earthx = new TextureImage("earth.jpg");
		jupiterx = new TextureImage("jupiter.jpg");
		saturnx = new TextureImage("saturn.jpg");
		neptunex = new TextureImage("neptune.jpg");
	}

	@Override
	public void buildObjects()
	{	Matrix4f initialTranslation, initialRotation, initialScale;

		plane = new GameObject(GameObject.root(), planeS);
		initialScale = (new Matrix4f()).scaling(10f);
		plane.setLocalScale(initialScale);
		(plane.getRenderStates()).setHasSolidColor(true);
		(plane.getRenderStates()).setColor(new Vector3f(0.0f, 0.0f, 0.0f));


		// build dolphin avatar
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(-1f,0.3f,1f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(135.0f));
		avatar.setLocalRotation(initialRotation);

		// build child objects for the avatar. set rendering state to false.
		childEarth = new GameObject(GameObject.root(), sphereS, earthx);
		initialTranslation = (new Matrix4f()).translation(.1f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(0.02f);
		childEarth.setLocalTranslation(initialTranslation);
		childEarth.setLocalScale(initialScale);
		childEarth.setParent(avatar);
		childEarth.propagateTranslation(true);
		childEarth.propagateRotation(true);
		childEarth.applyParentRotationToPosition(true);
		(childEarth.getRenderStates()).disableRendering();

		childJupiter = new GameObject(GameObject.root(), sphereS, jupiterx);
		initialTranslation = (new Matrix4f()).translation(.05f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(0.02f);
		childJupiter.setLocalTranslation(initialTranslation);
		childJupiter.setLocalScale(initialScale);
		childJupiter.setParent(avatar);
		childJupiter.propagateTranslation(true);
		childJupiter.propagateRotation(true);
		childJupiter.applyParentRotationToPosition(true);
		(childJupiter.getRenderStates()).disableRendering();

		childNeptune = new GameObject(GameObject.root(), sphereS, neptunex);
		initialTranslation = (new Matrix4f()).translation(-0.05f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(0.02f);
		childNeptune.setLocalTranslation(initialTranslation);
		childNeptune.setLocalScale(initialScale);
		childNeptune.setParent(avatar);
		childNeptune.propagateTranslation(true);
		childNeptune.propagateRotation(true);
		childNeptune.applyParentRotationToPosition(true);
		(childNeptune.getRenderStates()).disableRendering();

		childSaturn = new GameObject(GameObject.root(), sphereS, saturnx);
		initialTranslation = (new Matrix4f()).translation(-0.1f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(0.02f);
		childSaturn.setLocalRotation(initialRotation);
		childSaturn.setLocalTranslation(initialTranslation);
		childSaturn.setLocalScale(initialScale);
		childSaturn.setParent(avatar);
		childSaturn.propagateTranslation(true);
		childSaturn.propagateRotation(true);
		childSaturn.applyParentRotationToPosition(true);
		(childSaturn.getRenderStates()).disableRendering();


		// build earth in random location with random scale
		float randomX = generateRandomFloat(-8.0f, 8.0f);
		float randomZ = generateRandomFloat(-8.0f, 8.0f);
		float randomScale = generateRandomFloat(0.10f, 0.50f);
		earth = new GameObject(GameObject.root(), sphereS, earthx);
		initialTranslation = (new Matrix4f()).translation(randomX,0.5f,randomZ);
		earth.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(randomScale);
		earth.setLocalScale(initialScale);

		// build jupiter in random location with random scale
		randomX = generateRandomFloat(-8.0f, 8.0f);
		randomZ = generateRandomFloat(-8.0f, 8.0f);
		randomScale = generateRandomFloat(0.10f, 0.40f);
		jupiter = new GameObject(GameObject.root(), sphereS, jupiterx);
		initialTranslation = (new Matrix4f()).translation(randomX,0.5f,randomZ);
		jupiter.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(randomScale);
		jupiter.setLocalScale(initialScale);

		// build saturn in random location with random scale
		randomX = generateRandomFloat(-8.0f, 8.0f);
		randomZ = generateRandomFloat(-8.0f, 8.0f);
		randomScale = generateRandomFloat(0.10f, 0.40f);
		saturn = new GameObject(GameObject.root(), sphereS, saturnx);
		initialTranslation = (new Matrix4f()).translation(randomX,0.5f,randomZ);
		saturn.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(randomScale);
		saturn.setLocalScale(initialScale);

		// build neptune in random location with random scale
		randomX = generateRandomFloat(-8.0f, 8.0f);
		randomZ = generateRandomFloat(-8.0f, 8.0f);
		randomScale = generateRandomFloat(0.10f, 0.40f);
		neptune = new GameObject(GameObject.root(), sphereS, neptunex);
		initialTranslation = (new Matrix4f()).translation(randomX,0.5f,randomZ);
		neptune.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(randomScale);
		neptune.setLocalScale(initialScale);

		// build cube in random location with random scale
		cube = new GameObject(GameObject.root(), cubeS, cubex);
		initialTranslation = (new Matrix4f()).translation(0.0f,0.0f,0.0f);
		cube.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(10.0f);
		cube.setLocalScale(initialScale);

		// add X,Y,-Z axes
		x = new GameObject(GameObject.root(), linxS);
		y = new GameObject(GameObject.root(), linyS);
		z = new GameObject(GameObject.root(), linzS);
		(x.getRenderStates()).setColor(new Vector3f(1f,0f,0f));
		(y.getRenderStates()).setColor(new Vector3f(0f,1f,0f));
		(z.getRenderStates()).setColor(new Vector3f(0f,0f,1f));
		(x.getRenderStates()).enableRendering();
		(y.getRenderStates()).enableRendering();
		(z.getRenderStates()).enableRendering();
	}

	@Override
	public void createViewports()
	{	(engine.getRenderSystem()).addViewport("LEFT",0,0,1f,1f);
		(engine.getRenderSystem()).addViewport("RIGHT",.75f,0,.25f,.25f);

		Viewport leftVp = (engine.getRenderSystem()).getViewport("LEFT");
		Viewport rightVp = (engine.getRenderSystem()).getViewport("RIGHT");
		Camera leftCamera = leftVp.getCamera();
		Camera rightCamera = rightVp.getCamera();

		rightVp.setHasBorder(true);
		rightVp.setBorderWidth(4);
		rightVp.setBorderColor(0.0f, 1.0f, 0.0f);

		leftCamera.setLocation(new Vector3f(-2,0,2));
		leftCamera.setU(new Vector3f(1,0,0));
		leftCamera.setV(new Vector3f(0,1,0));
		leftCamera.setN(new Vector3f(0,0,-1));

		rightCamera.setLocation(new Vector3f(0,5,0));
		rightCamera.setU(new Vector3f(1,0,0));
		rightCamera.setV(new Vector3f(0,0,-1));
		rightCamera.setN(new Vector3f(0,-1,0));
	}


	@Override
	public void initializeGame()
	{	prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		rcY = new RotationController(engine, new Vector3f(0,1,0), 0.001f);
		rcZ = new RotationController(engine, new Vector3f(0,0,1), 0.001f);
		bc = new BounceController();
		rcY.toggle();
		rcZ.toggle();
		bc.toggle();
		(engine.getSceneGraph()).addNodeController(rcY);
		(engine.getSceneGraph()).addNodeController(rcZ);
		(engine.getSceneGraph()).addNodeController(bc);

		//----------------- adding light -----------------
		Light.setGlobalAmbient(.5f, .5f, .5f);

		light = new Light();
		light.setLocation(new Vector3f(0f, 5f, 0f));
		(engine.getSceneGraph()).addLight(light);

		// ----------------- initialize camera ----------------
		Camera camera = (engine.getRenderSystem()).getViewport("LEFT").getCamera(); 
  		//orbitController = new CameraOrbit3D(camera, avatar, engine); 

		// ----------------- INPUTS SECTION -----------------------------
		im = engine.getInputManager();

		// build some action objects for doing things in response to user input
		ZoomSecondCameraAction zoomCameraAction = new ZoomSecondCameraAction(this);
		PanSecondCameraAction panCameraAction = new PanSecondCameraAction(this);
		ToggleAxesAction toggleAxesAction = new ToggleAxesAction(this);

		/* Add all detected controllers to the input manager object */
		ArrayList<Controller> controllers = im.getControllers();
		orbitController = new CameraOrbit3D(camera, avatar, controllers, engine);
		for (Controller c : controllers)
		{ 
			if (c.getType() == Controller.Type.KEYBOARD)
			{
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.I, 
								   zoomCameraAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.K, 
								   zoomCameraAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.J, 
								   panCameraAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.L, 
								   panCameraAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.SPACE, 
								   toggleAxesAction,
				                   InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
			}
			else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK)
			{
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Button._4, 
								   panCameraAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Button._5, 
								   panCameraAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Axis.Z,
								   zoomCameraAction,
								   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
			}
		}
	}

	@Override
	public void update()
	{	
		didAvatarCollectItem();
		
		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		deltaTime = elapsedTime / 1000.0;
		dolphinLife -= deltaTime;
		int health = (int) dolphinLife;

		// build and set HUD
		int elapsTimeSec = Math.round((float)(System.currentTimeMillis()-startTime)/1000.0f);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String itemCnt = "   Score = " + Integer.toString(itemCount);
		String lifeCnt = " Health = " + Integer.toString(health);
		String dispStr1 = "Time = " + elapsTimeStr + itemCnt + lifeCnt;
		String dispStr2 = "Avatar position = "
			+ (avatar.getWorldLocation()).x()
			+ ", " + (avatar.getWorldLocation()).y()
			+ ", " + (avatar.getWorldLocation()).z();
		Vector3f hud1Color = new Vector3f(1,0,0);
		Vector3f hud2Color = new Vector3f(1,1,1);
		
		float leftVPWitdth = (engine.getRenderSystem()).getViewport("LEFT").getActualWidth();
		float rightVPWidth = (engine.getRenderSystem()).getViewport("RIGHT").getActualWidth();
		int relativeHUD2Pos = (int)(leftVPWitdth - rightVPWidth);
		(engine.getHUDmanager()).setHUD1(dispStr1, hud1Color, 15, 15);
		(engine.getHUDmanager()).setHUD2(dispStr2, hud2Color, relativeHUD2Pos, 15);
		
		if(dolphinLife <= 0.0f && itemCount < maxItemCnt) 
		{
			System.out.println("YOU LOST!");
			super.shutdown();
			System.exit(0);
			
		}
		if(dolphinLife > 0.0f && itemCount >= maxItemCnt) 
		{
			System.out.println("YOU WON!");
			super.shutdown();
			System.exit(0);
		}

		// update inputs and orbit controller camera
		im.update((float)elapsedTime);
		orbitController.setRotationAmount((float) elapsedTime);
		orbitController.updateCameraPosition(); 
	}

	
	/* Random Float Generator. Min and Max are inclusive. */
	private float generateRandomFloat(float min, float max)
	{
		Random rand = new Random();
		return min + rand.nextFloat() * (max - min);
	}

	/*
		Determine if the player has collected one of the game objects.
		If object is collected, increament item count until all 4 objects
		have been collected.
	*/
	public void didAvatarCollectItem()
	{
		Vector3f avaLoc = avatar.getWorldLocation();
		Vector3f earthLoc = earth.getWorldLocation();
		Vector3f jupiterLoc = jupiter.getWorldLocation();
		Vector3f saturnLoc = saturn.getWorldLocation();
		Vector3f neptuneLoc = neptune.getWorldLocation();

		float camEathhDistance = earthLoc.distance(avaLoc);
		float camJupiterDistance = jupiterLoc.distance(avaLoc);
		float camSaturnDistance = saturnLoc.distance(avaLoc);
		float camNeptuneDistance = neptuneLoc.distance(avaLoc);

		if(camEathhDistance < minDistance && !earthCollected)
		{
			if(itemCount < maxItemCnt) 
			{
				itemCount++;
				(childEarth.getRenderStates()).enableRendering();
				rcY.addTarget(earth);
				rcY.addTarget(childEarth);
				bc.addTarget(earth);
				earthCollected = true;
			}			
		}
		if(camJupiterDistance < minDistance && !jupiterCollected)
		{
			if(itemCount < maxItemCnt) 
			{
				itemCount++;
				(childJupiter.getRenderStates()).enableRendering();
				rcY.addTarget(jupiter);
				rcY.addTarget(childJupiter);
				bc.addTarget(jupiter);
				jupiterCollected = true;
			}			
		}
		if(camSaturnDistance < minDistance && !saturnCollected)
		{
			if(itemCount < maxItemCnt) 
			{
				itemCount++;
				(childSaturn.getRenderStates()).enableRendering();
				rcY.addTarget(saturn);
				rcY.addTarget(childSaturn);
				bc.addTarget(saturn);
				saturnCollected = true;
			
			}			
		}
		if(camNeptuneDistance < minDistance && !neptuneCollected)
		{
			if(itemCount < maxItemCnt) 
			{
				itemCount++;
				(childNeptune.getRenderStates()).enableRendering();
				rcZ.addTarget(neptune);
				rcZ.addTarget(childNeptune);
				bc.addTarget(neptune);
				neptuneCollected = true;
			}			
		}
	}
	
	public Engine getEngine() { return engine; }

	@Override
	public void keyPressed(KeyEvent e)
	{	
		super.keyPressed(e);
	}

	// ------------------ Additional Setters and Getters for new game attributes ------------------------------
	public double getDeltaTime() { return this.deltaTime; }
	public float getMaxDistance() { return this.maxDistance; }
	public GameObject getXAxisGameObject() { return this.x; }
	public GameObject getYAxisGameObject() { return this.y; }
	public GameObject getZAxisGameObject() { return this.z; }
}