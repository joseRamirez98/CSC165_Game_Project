package a2;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import tage.*;
import tage.shapes.*;
import tage.nodeControllers.*;
import tage.input.*;
import tage.input.action.*;

import java.lang.Math;
import java.awt.*;

import java.awt.event.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.*;
import org.joml.*;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier.*;
import java.util.concurrent.TimeUnit;
import tage.networking.IGameConnection.ProtocolType;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private CameraOrbit3D orbitController; 

	private Vector3f currentPosition;
	private double startTime, prevTime, elapsedTime, deltaTime;

	private GameObject childNeptune, neptune, childJupiter, jupiter, childSaturn, saturn, childEarth, earth, avatar, x, y, z,terr;
	private ObjShape sphereS, dolS, linxS, linyS, linzS, terrS, ghostS;
	private TextureImage earthx, saturnx, jupiterx, neptunex, doltx, hills,grass,desert, ghostT;
	private Light light;
	private NodeController rcY, rcZ, bc;

	private double earthLocX, earthLocZ, jupiterLocX, jupiterLocZ, saturnLocX, saturnLocZ, neptuneLocX, neptuneLocZ;

	/* New Attributes */
	private float minDistance; // Minimum distance to collect a game object.
	private float maxDistance; // The max distance the camera can be from dolphin.
	private float dolphinLife;
	private int itemCount;
	private int maxItemCnt;

	private int fluffyClouds,yellowClouds, lakeIslands,desertScape; //Skyboxes

	private boolean earthCollected;
	private boolean jupiterCollected;
	private boolean saturnCollected;
	private boolean neptuneCollected;

	private File scriptFile;
	ScriptEngine jsEngine;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;
	private GhostManager gm;

	public MyGame(String serverAddress, int serverPort, String protocol) 
	{
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		ghostS = new Sphere();
		terrS = new TerrainPlane(1000);
		sphereS = new Sphere();
		dolS = new ImportedModel("dolphinHighPoly.obj");
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures()
	{	
		ghostT = new TextureImage("redDolphin.jpg");
		doltx = new TextureImage("Dolphin_HighPolyUV.png");
		earthx = new TextureImage("earth.jpg");
		jupiterx = new TextureImage("jupiter.jpg");
		saturnx = new TextureImage("saturn.jpg");
		neptunex = new TextureImage("neptune.jpg");
		hills = new TextureImage("hills.jpg");
		//grass = new TextureImage("grass.jpg");
		desert = new TextureImage("desert.jpg");
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialRotation, initialScale;

		// utilize the script for initializations
		ScriptEngineManager factory = new ScriptEngineManager();
		java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
		jsEngine = factory.getEngineByName("js");
		scriptFile = new File("assets/scripts/init.js");
		this.runScript(scriptFile);
		earthLocX = ((double)(jsEngine.get("earthLocX")));
		earthLocZ = ((double)(jsEngine.get("earthLocZ")));
		jupiterLocX = ((double)(jsEngine.get("jupiterLocX")));
		jupiterLocZ = ((double)(jsEngine.get("jupiterLocZ")));
		saturnLocX = ((double)(jsEngine.get("saturnLocX")));
		saturnLocZ = ((double)(jsEngine.get("saturnLocZ")));
		neptuneLocX = ((double)(jsEngine.get("neptuneLocX")));
		neptuneLocZ = ((double)(jsEngine.get("neptuneLocZ")));
		float childPlantsScale = ((Double)(jsEngine.get("childPlantsScale"))).floatValue();
		float planetsScale = ((Double)(jsEngine.get("planetsScale"))).floatValue();

		// build dolphin avatar
		avatar = new GameObject(GameObject.root(), dolS, doltx);
		initialTranslation = (new Matrix4f()).translation(-1f,1f,1f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f));
		avatar.setLocalRotation(initialRotation);

		// build child objects for the avatar. set rendering state to false.
		childEarth = new GameObject(GameObject.root(), sphereS, earthx);
		initialTranslation = (new Matrix4f()).translation(.1f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(childPlantsScale);
		childEarth.setLocalTranslation(initialTranslation);
		childEarth.setLocalScale(initialScale);
		childEarth.setParent(avatar);
		childEarth.propagateTranslation(true);
		childEarth.propagateRotation(true);
		childEarth.applyParentRotationToPosition(true);
		(childEarth.getRenderStates()).disableRendering();

		childJupiter = new GameObject(GameObject.root(), sphereS, jupiterx);
		initialTranslation = (new Matrix4f()).translation(.05f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(childPlantsScale);
		childJupiter.setLocalTranslation(initialTranslation);
		childJupiter.setLocalScale(initialScale);
		childJupiter.setParent(avatar);
		childJupiter.propagateTranslation(true);
		childJupiter.propagateRotation(true);
		childJupiter.applyParentRotationToPosition(true);
		(childJupiter.getRenderStates()).disableRendering();

		childNeptune = new GameObject(GameObject.root(), sphereS, neptunex);
		initialTranslation = (new Matrix4f()).translation(-0.05f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(childPlantsScale);
		childNeptune.setLocalTranslation(initialTranslation);
		childNeptune.setLocalScale(initialScale);
		childNeptune.setParent(avatar);
		childNeptune.propagateTranslation(true);
		childNeptune.propagateRotation(true);
		childNeptune.applyParentRotationToPosition(true);
		(childNeptune.getRenderStates()).disableRendering();

		childSaturn = new GameObject(GameObject.root(), sphereS, saturnx);
		initialTranslation = (new Matrix4f()).translation(-0.1f,0f,-1.1f);
		initialScale = (new Matrix4f()).scaling(childPlantsScale);
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
		initialTranslation = (new Matrix4f()).translation((float)earthLocX,0.5f,(float)earthLocZ);
		earth.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(planetsScale);
		earth.setLocalScale(initialScale);

		// build jupiter in random location with random scale
		randomScale = generateRandomFloat(0.10f, 0.40f);
		jupiter = new GameObject(GameObject.root(), sphereS, jupiterx);
		initialTranslation = (new Matrix4f()).translation((float)jupiterLocX,0.5f,(float)jupiterLocZ);
		jupiter.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(planetsScale);
		jupiter.setLocalScale(initialScale);

		// build saturn in random location with random scale
		randomScale = generateRandomFloat(0.10f, 0.40f);
		saturn = new GameObject(GameObject.root(), sphereS, saturnx);
		initialTranslation = (new Matrix4f()).translation((float)saturnLocX,0.5f,(float)saturnLocZ);
		saturn.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(planetsScale);
		saturn.setLocalScale(initialScale);

		// build neptune in random location with random scale
		randomScale = generateRandomFloat(0.10f, 0.40f);
		neptune = new GameObject(GameObject.root(), sphereS, neptunex);
		initialTranslation = (new Matrix4f()).translation((float)neptuneLocX,0.5f,(float)neptuneLocZ);
		neptune.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(planetsScale);
		neptune.setLocalScale(initialScale);

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

		// build terraiun object
		//terr = new GameObject(GameObject.root(), terrS, grass);
		terr = new GameObject(GameObject.root(), terrS, desert);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		terr.setLocalTranslation(initialTranslation);
		//initialScale = (new Matrix4f()).scaling(20.0f,1.0f, 20.0f);
		initialScale = (new Matrix4f()).scaling(30.0f, 1.0f, 30.0f):
		terr.setLocalScale(initialScale);
		terr.setHeightMap(hills);
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
	public void loadSkyBoxes(){
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		//yellowClouds = (engine.getSceneGraph()).loadCubeMap("yellowClouds");
		//lakeIslands = (engine.getSceneGraph()).loadCubeMap("lakeIslands");
		desertScape = (engine.getSceneGraph()).loadCubeMap("desertScape");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		//(engine.getSceneGraph()).setActiveSkyBoxTexture(yellowClouds);
		//(engine.getSceneGraph()).setActiveSkyBoxTexture(lakeIslands);
		(engine.getSceneGraph()).setActiveSkyBoxTexture(desertScape);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}


	@Override
	public void initializeGame()
	{	prevTime = System.currentTimeMillis();
		startTime = System.currentTimeMillis();
		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// utilize the script for initializations
		ScriptEngineManager factory = new ScriptEngineManager();
		java.util.List<ScriptEngineFactory> list = factory.getEngineFactories();
		jsEngine = factory.getEngineByName("js");
		scriptFile = new File("assets/scripts/init.js");
		this.runScript(scriptFile);
		dolphinLife = ((Double)(jsEngine.get("dolphinLife"))).floatValue();
		minDistance = ((Double)(jsEngine.get("minDistance"))).floatValue();
		maxDistance = ((Double)(jsEngine.get("maxDistance"))).floatValue();
		itemCount = ((int)(jsEngine.get("itemCount")));
		maxItemCnt = ((int)(jsEngine.get("maxItemCnt")));
		earthCollected = ((boolean)(jsEngine.get("earthCollected")));
		jupiterCollected = ((boolean)(jsEngine.get("jupiterCollected")));
		saturnCollected = ((boolean)(jsEngine.get("saturnCollected")));
		neptuneCollected = ((boolean)(jsEngine.get("neptuneCollected")));

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

		// Setup the Protocol Client
		setupNetworking();

		// build some action objects for doing things in response to user input
		MoveAction moveAction = new MoveAction(this, protClient);
		TurnAction turnAction = new TurnAction(this, protClient);
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
								   net.java.games.input.Component.Identifier.Key.A, 
								   turnAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.D, 
								   turnAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.W, 
								   moveAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.S, 
								   moveAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
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
		// update altitude of dolphin based on height map
		Vector3f loc = avatar.getWorldLocation();
		float height = terr.getHeight(loc.x(), loc.z());
		avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));
		orbitController.updateCameraPosition(); 
		processNetworking((float)elapsedTime);
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

	private void runScript(File scriptFile)
	{	try
		{	FileReader fileReader = new FileReader(scriptFile);
			jsEngine.eval(fileReader);
			fileReader.close();
		}
		catch (FileNotFoundException e1)
		{	System.out.println(scriptFile + " not found " + e1); }
		catch (IOException e2)
		{	System.out.println("IO problem with " + scriptFile + e2); }
		catch (ScriptException e3) 
		{	System.out.println("ScriptException in " + scriptFile + e3); }
		catch (NullPointerException e4)
		{	System.out.println ("Null ptr exception reading " + scriptFile + e4);
		}
	}

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghostT; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking()
	{	isClientConnected = false;	
		try 
		{	protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e)
	{	
		switch(e.getKeyCode()){
			case KeyEvent.VK_1:
			{
				(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
				//(engine.getSceneGraph()).setActiveSkyBoxTexture(yellowClouds);
				(engine.getSceneGraph()).setSkyBoxEnabled(true);
				break;
			}
			case KeyEvent.VK_2:
			{
				//(engine.getSceneGraph()).setActiveSkyBoxTexture(lakeIslands);
				(engine.getSceneGraph()).setActiveSkyBoxTexture(desertScape);
				(engine.getSceneGraph()).setSkyBoxEnabled(true);
				break;
			}
			case KeyEvent.VK_3:
			{
				(engine.getSceneGraph()).setSkyBoxEnabled(false);
				break;
			}

		}
		super.keyPressed(e);
	}

	// ------------------ Additional Setters and Getters for new game attributes ------------------------------
	public GameObject getAvatar() { return avatar; }
	public double getDeltaTime() { return this.deltaTime; }
	public float getMaxDistance() { return this.maxDistance; }
	public void setAvatarPosition(Vector3f location) 
	{ 
		Matrix4f translation = (new Matrix4f()).translation(location);
		this.avatar.setLocalTranslation(translation); 
	}
	public GameObject getXAxisGameObject() { return this.x; }
	public GameObject getYAxisGameObject() { return this.y; }
	public GameObject getZAxisGameObject() { return this.z; }
	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }
}