package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;

public class PanSecondCameraAction extends AbstractInputAction
{
    private MyGame game;

    public PanSecondCameraAction(MyGame game)
    {
        this.game = game;
    }

    @Override
	public void performAction(float time, Event e)
	{ 
        String inputType = e.getComponent().getIdentifier().toString();
		Camera camera = (game.getEngine().getRenderSystem()).getViewport("RIGHT").getCamera();
        float deltaTime = (float) game.getDeltaTime();

        if(inputType == "L" || inputType == "5")
        {
            camera.moveNVector(-1.0f, deltaTime);
        }
        else if(inputType == "J" || inputType == "4")
        {
            camera.moveNVector(1.0f, deltaTime);
        }
    }
}