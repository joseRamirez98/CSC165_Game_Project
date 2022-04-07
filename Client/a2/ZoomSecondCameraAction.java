package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class ZoomSecondCameraAction extends AbstractInputAction
{
    private MyGame game;

    public ZoomSecondCameraAction(MyGame game)
    {
        this.game = game;
    }

    @Override
	public void performAction(float time, Event e)
	{ 
        String inputType = e.getComponent().getIdentifier().toString();
		Camera camera = (game.getEngine().getRenderSystem()).getViewport("RIGHT").getCamera();
        float deltaTime = (float) game.getDeltaTime();

        if(inputType == "I")
        {
            camera.move(-1.0f, deltaTime);
        }
        else if(inputType == "K")
        {
            camera.move(1.0f, deltaTime);
        }
        else
        {
			float keyValue = e.getValue();
            camera.move(keyValue, deltaTime);
        }
    }
}