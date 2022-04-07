package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import org.joml.*;

public class TurnAction extends AbstractInputAction
{
	private MyGame game;
	private GameObject av;
	private ProtocolClient protClient;


	public TurnAction(MyGame g, ProtocolClient p)
	{	game = g;
		protClient = p;
	}

	@Override
	public void performAction(float time, Event e)
	{
		String inputType = e.getComponent().getIdentifier().toString();
		Camera camera = (game.getEngine().getRenderSystem()).getViewport("LEFT").getCamera();
		float deltaTime = (float) game.getDeltaTime();
		av = game.getAvatar();

		if(inputType == "D"){ //player turns right
			av.yaw(1.0f, deltaTime);
			game.didAvatarCollectItem();
		}
		else if(inputType == "A"){ //player turns left
			av.yaw(-1.0f, deltaTime);
			game.didAvatarCollectItem();
		}
		else { // player turns based on X Axis Controller input
			float keyValue = e.getValue();
			// Determine if player is on or off the dolphin
			av.yaw(keyValue, deltaTime);
			game.didAvatarCollectItem();
		}

		protClient.sendMoveMessage(av.getWorldLocation());
	}
}


