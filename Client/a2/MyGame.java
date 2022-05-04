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

import tage.audio.*;
import com.jogamp.openal.ALFactory;

import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.PhysicsEngineFactory;
import tage.physics.JBullet.*;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.collision.dispatch.CollisionObject;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private CameraOrbit3D orbitController; 

	private Vector3f currentPosition;
	private double startTime, prevTime, elapsedTime, deltaTime;

	private GameObject coin, coinTwo, coinThree, coinFour, childCoinTwo, childCoinThree, childCoinFour, avatar, avatarTwo, x, y, z,terr;
	private ObjShape coinS, coinTwoS, avatarS, avaTwoS, linxS, linyS, linzS, terrS, ghostS;
	private AnimatedShape avaS; 
	private TextureImage coinX, coinTwoX, avatarTwox, carx, hills,desert, ghostT;
	private Light light;
	private NodeController rcY, rcZ, bc;

	/* Audio Sound*/
	private IAudioManager audioMgr;
	private Sound coinSound, blueCoinSound,nascarRacersExtSound, NascarRacersThemeSound,finalLapSound, gameSound, gotitemSound, lapSound, levelupSound, lowhealthSound, sboomSound, gameoverSound, thud1Sound, titleSound, weirdhitSound, winnerSound;

	private PhysicsEngine physicsEngine;
	private PhysicsObject avatarP, avatarTwoP, planeP;
	private boolean running = false;
	private float vals[] = new float[16];

	/* New Attributes */
	private float minDistance; // Minimum distance to collect a game object.
	private float maxDistance; // The max distance the camera can be from dolphin.
	private float dolphinLife;
	private int coinCount;
	private int lapCount=0;
	private int maxCoinCount;

	private boolean coinOneCollected = true;
	private boolean coinTwoCollected = false;
	private boolean coinThreeCollected = false;
	private boolean coinFourCollected = false;

	private int fluffyClouds,yellowClouds, lakeIslands,desertScape; //Skyboxes

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
		avatarS = new ImportedModel("car2.obj");
		// avaS = new AnimatedShape("car.rkm", "car_skeleton.rks");
		// avaS.loadAnimation("LEFT", "left_turn.rka"); 
  		// avaS.loadAnimation("RIGHT", "right_turn.rka"); 
		avaTwoS = new ImportedModel("car.obj");
		coinS = new ImportedModel("coin.obj");
		coinTwoS = new ImportedModel("coin.obj");
		linxS = new Line(new Vector3f(0f,0f,0f), new Vector3f(3f,0f,0f));
		linyS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,3f,0f));
		linzS = new Line(new Vector3f(0f,0f,0f), new Vector3f(0f,0f,-3f));
	}

	@Override
	public void loadTextures()
	{	
		ghostT = new TextureImage("redDolphin.jpg");
		carx = new TextureImage("car2.jpg");
		avatarTwox = new TextureImage("car.jpg");
		hills = new TextureImage("hills3.jpg");
		desert = new TextureImage("desert.jpg");
		coinX = new TextureImage("coin.jpg");
		coinTwoX = new TextureImage("coin.jpg");
	}

	@Override
	public void buildObjects()
	{	
		Matrix4f initialTranslation, initialRotation, initialScale;


		// build dolphin avatar
		// use avatarS for Imported Model and avaS for Animated Model
		avatar = new GameObject(GameObject.root(), avatarS , carx); 
		initialTranslation = (new Matrix4f()).translation(0f,0.5f, -10f);
		avatar.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(0.0f));
		avatar.setLocalRotation(initialRotation);

		avatarTwo = new GameObject(GameObject.root(), avaTwoS, avatarTwox);
		initialTranslation = (new Matrix4f()).translation(0f,0f,-12f);
		avatarTwo.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationY((float)java.lang.Math.toRadians(90.0f));
		avatarTwo.setLocalRotation(initialRotation);

		coin = new GameObject(GameObject.root(), coinS, coinX);
		initialTranslation = (new Matrix4f()).translation(0f,.5f,-11f);
		coin.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationZ((float)java.lang.Math.toRadians(90.0f));
		coin.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.5f);
		coin.setLocalScale(initialScale);
		(coin.getRenderStates()).disableRendering();

		coinTwo = new GameObject(GameObject.root(), coinTwoS, coinTwoX);
		initialTranslation = (new Matrix4f()).translation(0f,.5f, 11f);
		coinTwo.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationZ((float)java.lang.Math.toRadians(90.0f));
		coinTwo.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.5f);
		coinTwo.setLocalScale(initialScale);

		coinThree = new GameObject(GameObject.root(), coinTwoS, coinTwoX);
		initialTranslation = (new Matrix4f()).translation(-14f,.5f, 0f);
		coinThree.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));
		coinThree.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.5f);
		coinThree.setLocalScale(initialScale);

		coinFour = new GameObject(GameObject.root(), coinS, coinX);
		initialTranslation = (new Matrix4f()).translation(14f,.5f, 0f);
		coinFour.setLocalTranslation(initialTranslation);
		initialRotation = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));
		coinFour.setLocalRotation(initialRotation);
		initialScale = (new Matrix4f()).scaling(0.5f);
		coinFour.setLocalScale(initialScale);

		// build child objects for the avatar. set rendering state to false.
		childCoinTwo = new GameObject(GameObject.root(), coinS, coinX);
		initialTranslation = (new Matrix4f()).translation(0.1f,0.25f,-1.1f);
		initialRotation = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));
		initialScale = (new Matrix4f()).scaling(0.2f);
		childCoinTwo.setLocalTranslation(initialTranslation);
		childCoinTwo.setLocalScale(initialScale);
		childCoinTwo.setLocalRotation(initialRotation);
		childCoinTwo.setParent(avatar);
		childCoinTwo.propagateTranslation(true);
		childCoinTwo.propagateRotation(true);
		childCoinTwo.applyParentRotationToPosition(true);
		(childCoinTwo.getRenderStates()).disableRendering();

		childCoinThree = new GameObject(GameObject.root(), coinS, coinX);
		initialTranslation = (new Matrix4f()).translation(0f,0.25f,-1.1f);
		initialRotation = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));
		initialScale = (new Matrix4f()).scaling(0.2f);
		childCoinThree.setLocalTranslation(initialTranslation);
		childCoinThree.setLocalRotation(initialRotation);
		childCoinThree.setLocalScale(initialScale);
		childCoinThree.setParent(avatar);
		childCoinThree.propagateTranslation(true);
		childCoinThree.propagateRotation(true);
		childCoinThree.applyParentRotationToPosition(true);
		(childCoinThree.getRenderStates()).disableRendering();

		childCoinFour = new GameObject(GameObject.root(), coinS, coinX);
		initialTranslation = (new Matrix4f()).translation(-0.1f,0.25f,-1.1f);
		initialRotation = (new Matrix4f()).rotationX((float)java.lang.Math.toRadians(90.0f));
		initialScale = (new Matrix4f()).scaling(0.2f);
		childCoinFour.setLocalTranslation(initialTranslation);
		childCoinFour.setLocalRotation(initialRotation);
		childCoinFour.setLocalScale(initialScale);
		childCoinFour.setParent(avatar);
		childCoinFour.propagateTranslation(true);
		childCoinFour.propagateRotation(true);
		childCoinFour.applyParentRotationToPosition(true);
		(childCoinFour.getRenderStates()).disableRendering();

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
		terr = new GameObject(GameObject.root(), terrS, desert);
		initialTranslation = (new Matrix4f()).translation(0f,0f,0f);
		terr.setLocalTranslation(initialTranslation);
		initialScale = (new Matrix4f()).scaling(20.0f,1.0f, 20.0f);
		terr.setLocalScale(initialScale);
		terr.getRenderStates().hasLighting(true);
		terr.setHeightMap(hills); //THIS IS FOR TERRAIN PLANE
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
		desertScape = (engine.getSceneGraph()).loadCubeMap("desertScape");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
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
		coinCount = ((int)(jsEngine.get("coinCount")));
		maxCoinCount = ((int)(jsEngine.get("maxCoinCount")));
		float avatarScale = ((Double)(jsEngine.get("avatarScale"))).floatValue();

		rcZ = new RotationController(engine, new Vector3f(0,0,1), 0.001f);
		bc = new BounceController();
		rcZ.toggle();
		bc.toggle();
		(engine.getSceneGraph()).addNodeController(rcZ);
		(engine.getSceneGraph()).addNodeController(bc);
		rcZ.addTarget(childCoinTwo);
		rcZ.addTarget(childCoinThree);
		rcZ.addTarget(childCoinFour);

		//----------------- adding light -----------------
		Light.setGlobalAmbient(.5f, .5f, .5f);

		light = new Light();
		light.setLocation(new Vector3f(0f, 5f, 0f));
		(engine.getSceneGraph()).addLight(light);

		// ----------------- initialize camera ----------------
		Camera camera = (engine.getRenderSystem()).getViewport("LEFT").getCamera();

		//------------- PHYSICS --------------
		//     --- initialize physics system ---
		String enginePath = "tage.physics.JBullet.JBulletPhysicsEngine";
		float[] gravity = {0f, -10f, 0f};
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(enginePath);
		physicsEngine.initSystem();
		physicsEngine.setGravity(gravity);

		//     --- create physics world ---
		float mass = 1.0f;
		float up[] = {0,1,0};
		double[] tempTransform;

		Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
		float halfExtents[] = {0.5f, 0.5f, 0.5f};
		tempTransform = toDoubleArray(translation.get(vals));
		avatarP = physicsEngine.addBoxObject(0, mass, tempTransform, halfExtents);
		avatarP.setBounciness(0.0f);
		avatar.setPhysicsObject(avatarP);
		Matrix4f initialScale = (new Matrix4f()).scaling(avatarScale);
		avatar.setLocalScale(initialScale);

		translation = new Matrix4f(avatarTwo.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		avatarP = physicsEngine.addBoxObject(1, mass, tempTransform, halfExtents);
		avatarP.setBounciness(0.0f);
		avatarTwo.setPhysicsObject(avatarP);
		initialScale = (new Matrix4f()).scaling(avatarScale);
		avatarTwo.setLocalScale(initialScale);

		translation = terr.getLocalTranslation();
		tempTransform = toDoubleArray(translation.get(vals));
		planeP = physicsEngine.addStaticPlaneObject(2, tempTransform, up, 0.0f);
		planeP.setBounciness(0.0f);
		terr.setPhysicsObject(planeP);

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
		/*------------- AUDIO SECTION ---------*/
		initAudio();
	}
	
	/* Initialize Audio Sounds*/
	public void initAudio(){
		AudioResource resource1, resource2, resource3, resource4, resource5, resource6, resource7, resource8, resource9, resource10, resource11, resource12, resource13;
		audioMgr = AudioManagerFactory.createAudioManager("tage.audio.joal.JOALAudioManager");
		if(!audioMgr.initialize()){
			System.out.println("Audio Manager failed to initialize");
			return;
		}
		resource1 = audioMgr.createAudioResource("assets/sounds/title.wav", AudioResourceType.AUDIO_STREAM);
		//resource2 = audioMgr.createAudioResource("assets/sounds/game.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = audioMgr.createAudioResource("assets/sounds/coin.wav", AudioResourceType.AUDIO_SAMPLE);
		resource4 = audioMgr.createAudioResource("assets/sounds/lap.wav", AudioResourceType.AUDIO_SAMPLE);
		/**resource5 = audioMgr.createAudioResource("assets/sounds/levelup.wav", AudioResourceType.AUDIO_SAMPLE);
		resource6 = audioMgr.createAudioResource("assets/sounds/lowhealth.wav", AudioResourceType.AUDIO_SAMPLE);
		resource7 = audioMgr.createAudioResource("assets/sounds/gotitem.wav", AudioResourceType.AUDIO_SAMPLE);
		resource8 = audioMgr.createAudioResource("assets/sounds/sboom.wav", AudioResourceType.AUDIO_SAMPLE);
		resource9 = audioMgr.createAudioResource("assets/sounds/thud1.wav", AudioResourceType.AUDIO_SAMPLE);
		resource10 = audioMgr.createAudioResource("assets/sounds/weirdhit.wav", AudioResourceType.AUDIO_SAMPLE);
		resource11 = audioMgr.createAudioResource("assets/sounds/finalLap.wav", AudioResourceType.AUDIO_SAMPLE);
		resource12 = audioMgr.createAudioResource("assets/sounds/winner.wav", AudioResourceType.AUDIO_SAMPLE);
		resource13 = audioMgr.createAudioResource("assets/sounds/bluecoin.wav", AudioResourceType.AUDIO_SAMPLE);
		resource14 = audioMgr.createAudioResource("assets/sounds/gameover.wav", AudioResourceType.AUDIO_SAMPLE);
		**/
		
		titleSound = new Sound(resource1, SoundType.SOUND_MUSIC, 100, true);
		//gameSound = new Sound(resource2, SoundType.SOUND_EFFECT, 100, true);
		coinSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, true);
	/**	lapSound = new Sound(resource4, SoundType.SOUND_EFFECT, 100, true);
		levelupSound = new Sound(resource5, SoundType.SOUND_EFFECT, 100, true);
		lowhealthSound = new Sound(resource6, SoundType.SOUND_EFFECT, 100, true);
		gotitemSound = new Sound(resource7, SoundType.SOUND_EFFECT, 100, true);
		sboomSound = new Sound(resource8, SoundType.SOUND_EFFECT, 100, true);
		thud1Sound = new Sound(resource9, SoundType.SOUND_EFFECT, 100, true);
		weirdhitSound = new Sound(resource10, SoundType.SOUND_EFFECT, 100, true);
		finalLapSound = new Sound(resource11, SoundType.SOUND_EFFECT, 100, true);
		winnerSound = new Sound(resource12, SoundType.SOUND_EFFECT, 100, true);
		gameoverSound = new Sound(resource13, SoundType.SOUND_EFFECT, 100, true);
		
		**/
		
		titleSound.initialize(audioMgr);
		/**gameSound.initialize(audioMgr);**/
		coinSound.initialize(audioMgr);
		titleSound.setMaxDistance(10.0f);
		titleSound.setMinDistance(0.5f);
		titleSound.setRollOff(5.0f);
		/**gameSound.setMaxDistance(10.0f);
		gameSound.setMinDistance(0.5f);
		gameSound.setRollOff(5.0f);**/
		
		coinSound.setMaxDistance(10.0f);
		coinSound.setMinDistance(0.5f);
		coinSound.setRollOff(5.0f);
		
	/*	lapSound.setMaxDistance(10.0f);
		lapSound.setMinDistance(0.5f);
		lapSound.setRollOff(5.0f);*/
		
		titleSound.setLocation(avatar.getWorldLocation());
		//gameSound.setLocation(terr.getWorldLocation());
		//coinSound.setLocation(coin.getWorldLocation());
		/**coinSound.setLocation(coinTwo.getWorldLocation());
		coinSound.setLocation(coinThree.getWorldLocation());
		coinSound.setLocation(coinFour.getWorldLocation());**/
		setEarParameters();
		
		titleSound.play();
		//gameSound.play();
		//coinSound.play();
	}
	
	/*------- Set Ear Parameters ---------*/
	public void setEarParameters(){
		Camera cam = (engine.getRenderSystem()).getViewport("LEFT").getCamera();
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(cam.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
		
	}

	@Override
	public void update()
	{	
		// IGNORE FOR NOW
		// didAvatarCollectItem();

		elapsedTime = System.currentTimeMillis() - prevTime;
		prevTime = System.currentTimeMillis();
		deltaTime = elapsedTime / 1000.0;
		int health = (int) dolphinLife;

		// build and set HUD
		int elapsTimeSec = Math.round((float)(System.currentTimeMillis()-startTime)/1000.0f);
		String elapsTimeStr = Integer.toString(elapsTimeSec);
		String itemCnt = "   Item Count = " + Integer.toString(coinCount);
		String lifeCnt = " Lap = " + Integer.toString(lapCount);
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
		
		// update inputs
		im.update((float)elapsedTime);
	
		// update physics
		if (running)
		{	
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			//Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float)elapsedTime);
			for (GameObject go:engine.getSceneGraph().getGameObjects())
			{	if (go.getPhysicsObject() != null)
				{	
					// Set the phsyics obj translation to the graphics obj translation
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30()); mat2.set(3,1,mat.m31()); mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);
					
					// Set the phsyics obj rotation to the graphics obj rotation
					mat2.set(2,0,mat.m20()); mat2.set(2,1,mat.m21()); mat2.set(2,2,mat.m22());
					mat2.set(1,0,mat.m10()); mat2.set(1,1,mat.m11()); mat2.set(1,2,mat.m12());
					AxisAngle4f aa = new AxisAngle4f();
      				mat2.getRotation(aa);
					Matrix4f rotMatrix = new Matrix4f();
      				rotMatrix.rotation(aa);
					go.setLocalRotation(rotMatrix);
				}
			}
		}
		orbitController.setRotationAmount((float) elapsedTime);
		orbitController.updateCameraPosition();
		//avaS.updateAnimation(); 
		
		// update Sound
		titleSound.setLocation(avatar.getWorldLocation());
		//gameSound.setLocation(terr.getWorldLocation());
		//coinSound.setLocation(coin.getWorldLocation());
		/**coinSound.setLocation(coinTwo.getWorldLocation());
		coinSound.setLocation(coinThree.getWorldLocation());
		coinSound.setLocation(coinFour.getWorldLocation());**/
		setEarParameters();
		
		processNetworking((float)elapsedTime);
	}
 
	private void checkForCollisions()
	{	com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

		dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i=0; i<manifoldCount; i++)
		{	manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++)
			{	contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.0f)
				{	//System.out.println("---- hit between OBJ:" + obj1.getUID() + " and OBJ:" + obj2.getUID());
					break;
				}
			}
		}
	}

	public void addPhysicsToObject(GameObject obj)
	{
		Matrix4f objTranslation = new Matrix4f(obj.getLocalTranslation());
		float halfExtents[] = {1.0f, 1.0f, 1.0f};
		double[] tempTransform;
		float mass = 1.0f;

		tempTransform = toDoubleArray(objTranslation.get(vals));
		PhysicsObject objP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass, tempTransform, halfExtents);
		obj.setPhysicsObject(objP);
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
		Vector3f coinLoc = coin.getWorldLocation();
		Vector3f coinTwoLoc = coinTwo.getWorldLocation();
		Vector3f coinThreeLoc = coinThree.getWorldLocation();
		Vector3f coinFourLoc = coinFour.getWorldLocation();

		float distance = coinLoc.distance(avaLoc);
		float distanceTwo = coinTwoLoc.distance(avaLoc);
		float distanceThree = coinThreeLoc.distance(avaLoc);
		float distanceFour = coinFourLoc.distance(avaLoc);

		if(distance < minDistance)
		{
			if (coinCount == 3)
			{
				coinCount = 1;
				lapCount++;
				//lapSound.setLocation(lapCount);
				//lapSound.play();
				coinOneCollected = true;
				coinTwoCollected = false;
				coinThreeCollected = false;
				coinFourCollected = false;
				(childCoinTwo.getRenderStates()).disableRendering();
				(childCoinThree.getRenderStates()).disableRendering();
				(childCoinFour.getRenderStates()).disableRendering();
				(coin.getRenderStates()).disableRendering();
				(coinTwo.getRenderStates()).enableRendering();
				(coinThree.getRenderStates()).enableRendering();
				(coinFour.getRenderStates()).enableRendering();
				coinSound.setLocation(coin.getWorldLocation());
			}			
		}

		if(distanceTwo < minDistance && !coinTwoCollected)
		{
			if(coinCount < maxCoinCount) 
			{
				coinCount++;
				(childCoinTwo.getRenderStates()).enableRendering();
				(coinTwo.getRenderStates()).disableRendering();
				coinTwoCollected = true;
				coinSound.setLocation(coinTwo.getWorldLocation());
				coinSound.play();
			}else{
				coinSound.stop();
			}
		}

		if(distanceThree < minDistance && !coinThreeCollected)
		{
			if(coinCount < maxCoinCount) 
			{
				coinCount++;
				(childCoinThree.getRenderStates()).enableRendering();
				(coinThree.getRenderStates()).disableRendering();
				coinThreeCollected = true;
				coinSound.setLocation(coinThree.getWorldLocation());
				//coinSound.play();
			}			
		}
			
		if(distanceFour < minDistance && !coinFourCollected)
		{
			if(coinCount < maxCoinCount) 
			{
				coinCount++;
				(childCoinFour.getRenderStates()).enableRendering();
				(coinFour.getRenderStates()).disableRendering();
				coinFourCollected = true;
				coinSound.setLocation(coinFour.getWorldLocation());
				//coinSound.play();
			}			
		}

		if (coinCount == 3)
		{
			(coin.getRenderStates()).enableRendering();
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

	// ------------------ UTILITY FUNCTIONS used by physics

	private float[] toFloatArray(double[] arr)
	{	if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++)
		{	ret[i] = (float)arr[i];
		}
		return ret;
	}
 
	private double[] toDoubleArray(float[] arr)
	{	if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++)
		{	ret[i] = (double)arr[i];
		}
		return ret;
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

	// ------------------ Additional Setters and Getters for new game attributes ------------------------------
	public GameObject getAvatar() { return avatar; }
	public AnimatedShape getAvatarShape() { return avaS; }
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