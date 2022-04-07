package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class ToggleAxesAction extends AbstractInputAction
{
    private MyGame game;

    public ToggleAxesAction(MyGame game)
    {
        this.game = game;
    }

    @Override
	public void performAction(float time, Event e)
	{
        boolean xState = ((game.getXAxisGameObject()).getRenderStates()).renderingEnabled();
        boolean yState = ((game.getYAxisGameObject()).getRenderStates()).renderingEnabled();
        boolean zState = ((game.getZAxisGameObject()).getRenderStates()).renderingEnabled();
        if(xState && yState && zState)
        {
            ((game.getXAxisGameObject()).getRenderStates()).disableRendering();
            ((game.getYAxisGameObject()).getRenderStates()).disableRendering();
            ((game.getZAxisGameObject()).getRenderStates()).disableRendering();
        }
        else
        {
            ((game.getXAxisGameObject()).getRenderStates()).enableRendering();
            ((game.getYAxisGameObject()).getRenderStates()).enableRendering();
            ((game.getZAxisGameObject()).getRenderStates()).enableRendering();
        }
    }

}