package tage.nodeControllers;
import tage.*;
import org.joml.*;

/**
* A BounceController is a node controller that, when enabled, causes any object
* it is attached to, to move up and down around the Y axis.
*/
public class BounceController extends NodeController
{
	private Matrix4f translation;
	private Vector3f location;
	private boolean isObjectRising;

    /** Creates a bounce controller.*/
	public BounceController()
	{	super();
		isObjectRising = true;
	}
	
	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
    public void apply(GameObject go)
    {
		float elapsedTime = super.getElapsedTime()/3000.0f;
		float deltaY = 0.0f;
        location = go.getWorldLocation();

		if(location.y() >= 1.0f)
		{
			isObjectRising = false;
		}
		if(location.y() <= 0.3f)
		{
			isObjectRising = true;
		}

		if(isObjectRising)
		{
			deltaY = location.y() + elapsedTime;
		}
		else
		{
			deltaY = location.y() + (elapsedTime*-1.0f);
		}

		translation = (new Matrix4f()).translation(location.x(), deltaY,location.z());
		go.setLocalTranslation(translation);
    }
}