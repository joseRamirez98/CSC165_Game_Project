package tage;

import tage.*;
import tage.input.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;
import java.util.ArrayList;
import java.lang.Math;
import net.java.games.input.*;
import java.util.ArrayList;

/**
* A 3D camera obrit controller defines the camera movement around a target.
* <br>
* Specifically, the orbit controller includes the following:
* <ul>
* <li> a link to the game engine
* <li> a link to the camera
* <li> a link to a game object (target)
* <li> float variable that defines the rotation of the camera around the target y axis
* <li> float variable that defines the elevation of the camera above the target
* <li> float variable that defines the distance between the camera and the target
* <li> float variable that defines the amount of rotation
* <li> float variable that defines the limit a camera can zoom in on the target
* <li> float variable that defines the limit a camera can zoom out from the target
*/
public class CameraOrbit3D
{	private Engine engine;
	private Camera camera;		//the camera being controlled
	private GameObject avatar;	//the target avatar the camera looks at
	private float cameraAzimuth;	//rotation of camera around target Y axis
	private float cameraElevation;	//elevation of camera above target
	private float cameraRadius;	//distance between camera and target
    private float rotAmount;
    private float zoomInLimit = 1.0f;
    private float zoomOutLimit = 3.0f;

	/** Create a camera orbit controller with a given camera, game object, and game engine. */
	public CameraOrbit3D(Camera cam, GameObject av, ArrayList<Controller> controllers, Engine e)
	{	engine = e;
		camera = cam;
		avatar = av;
		cameraAzimuth = 0.0f;		// start from BEHIND and ABOVE the target
		cameraElevation = 20.0f;	// elevation is in degrees
		cameraRadius = 2.0f;		// distance from camera to avatar
		setupInputs(controllers);
		updateCameraPosition();
	}

	/** Sets the amount of rotation for the orbit controller. */
    public void setRotationAmount(float rotAmount) { this.rotAmount = rotAmount; }

	private void setupInputs(ArrayList<Controller> controllers)
	{	
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction();
        OrbitRadiusAction radiusAction = new OrbitRadiusAction();
        OrbitElevationAction elevationAction = new OrbitElevationAction();
        AvatarYawAction yawAction = new AvatarYawAction();
        AvatarFowardAction forwardAction = new AvatarFowardAction();

		InputManager im = engine.getInputManager();
		
        for (Controller c : controllers)
		{ 
            if (c.getType() == Controller.Type.KEYBOARD)
			{
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.W, 
								   forwardAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.S, 
								   forwardAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.A, 
								   yawAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.D, 
								   yawAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);                   
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.Z, 
								   radiusAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.X, 
								   radiusAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.UP, 
								   elevationAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.DOWN, 
								   elevationAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.RIGHT, 
								   azmAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
                im.associateAction(c,
								   net.java.games.input.Component.Identifier.Key.LEFT, 
								   azmAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            }
            else if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK)
			{ 
                im.associateAction(c,
                                   net.java.games.input.Component.Identifier.Axis.RX,
                                   azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Axis.RY, 
								   elevationAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Axis.Y, 
								   forwardAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Axis.X, 
								   yawAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Button._0, 
								   radiusAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
				im.associateAction(c,
								   net.java.games.input.Component.Identifier.Button._3, 
								   radiusAction,
				                   InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            }
        }
	}

	/** Updates the camera's position relative to the target. */
	// Updates the camera position by computing its azimuth, elevation, and distance 
	// relative to the target in spherical coordinates, then converting those spherical 
	// coords to world Cartesian coordinates and setting the camera position from that.
	public void updateCameraPosition()
	{	Vector3f avatarRot = avatar.getWorldForwardVector();
		double avatarAngle = Math.toDegrees((double)avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));
		float totalAz = cameraAzimuth - (float)avatarAngle;
		double theta = Math.toRadians(totalAz);	// rotation around target
		double phi = Math.toRadians(cameraElevation);	// altitude angle
		float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
		float y = cameraRadius * (float)(Math.sin(phi));
		float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));
		
		camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation()));
		camera.lookAt(avatar);
	}

	private class OrbitAzimuthAction extends AbstractInputAction
	{	public void performAction(float time, Event event)
		{	
	
            String inputType = event.getComponent().getIdentifier().toString();
			float keyValue = event.getValue();
			rotAmount /= 100.0f;

			if (keyValue > -.2 && keyValue < .2) // dead zone on joystick
			{
				rotAmount = 0.0f;
			}
			else if(inputType == "Left")
			{
				rotAmount *=-1.0f;
			}
			else if(inputType == "Right")
			{
 				rotAmount *= 1.0f;
			}
			else
			{
				if(keyValue < -0.2)
				{	
					rotAmount *=-1.0f;
				}
				else
				{
					rotAmount *= 1.0;
				}
			}

			cameraAzimuth += rotAmount;
			cameraAzimuth = cameraAzimuth % 360;
			updateCameraPosition();
		}
	}
  
	private class OrbitRadiusAction extends AbstractInputAction
    {
        public void performAction(float time, Event event)
		{
            String inputType = event.getComponent().getIdentifier().toString();
            rotAmount /= 1000.0f;
            
            if(inputType == "Z" || inputType == "3")
			{	
                rotAmount *= -1.0f;
			}

			cameraRadius += rotAmount;
			cameraRadius = cameraRadius % 360;
			updateCameraPosition();
        }
    }
  
	private class OrbitElevationAction extends AbstractInputAction
	{
        public void performAction(float time, Event event)
		{
            String inputType = event.getComponent().getIdentifier().toString();
			float keyValue = event.getValue();
            rotAmount /= 100.0f;

			if(inputType == "Down")
            {	
                rotAmount *= -1.0f;
            }
			else if(inputType == "Up")
			{
				rotAmount *= 1.0f;
			}
			else
			{
				if(keyValue > 0.2)
				{
					rotAmount *= -1.0f;
				}
				else
				{
					if(keyValue < -0.2f)
					{
						rotAmount *= 1.0f;
					}
					else
					{
						rotAmount = 0.0f;
					}
				}
			}
			
			float tempElevation = cameraElevation + rotAmount;
            if(tempElevation > 0f)
			{
				cameraElevation += rotAmount;
				cameraElevation = cameraElevation % 360;
			}

			
            updateCameraPosition();
        }
    }

    private class AvatarYawAction extends AbstractInputAction
    {
        public void performAction(float time, Event event)
        {
            String inputType = event.getComponent().getIdentifier().toString();
            rotAmount /= 1000.f;

            if(inputType == "A") 
            {
				avatar.yaw(-1.0f, rotAmount);
            }
            else if(inputType == "D")
            {	
                avatar.yaw(1.0f, rotAmount);
            }
			else{
				float keyValue = event.getValue();
				avatar.yaw(keyValue, rotAmount);
			}
        }
    }

    private class AvatarFowardAction extends AbstractInputAction
    {
        public void performAction(float time, Event event)
        {
            String inputType = event.getComponent().getIdentifier().toString();
            rotAmount /= 1000.f;

            if(inputType == "W") 
            {
				avatar.move(-1.0f, rotAmount);
            }
            else if(inputType == "S")
            {	
                avatar.move(1.0f, rotAmount);
            }
			else
			{
				float keyValue = event.getValue();
				avatar.move(keyValue, rotAmount);
			}
        }
    }
}