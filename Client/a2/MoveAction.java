package a2;

import tage.*;
import tage.input.action.AbstractInputAction;
import net.java.games.input.Event;
import net.java.games.input.*;
import org.joml.*;

public class MoveAction extends AbstractInputAction
{
	private MyGame game;
	private GameObject av;
	private ProtocolClient protClient;

	public MoveAction(MyGame g, ProtocolClient p)
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


		if(inputType == "W") {
			// Determine if player is on or off the dolphin
			av.move(-1.0f, deltaTime);
			game.didAvatarCollectItem();
		}
		else if(inputType == "S") {
			// Determine if player is on or off the dolphin
			av.move(1.0f, deltaTime);
			game.didAvatarCollectItem();
		}
		else{
			float keyValue = e.getValue();
			// Determine if player is on or off the dolphin
			av.move(keyValue, deltaTime);
			game.didAvatarCollectItem();
		}
		protClient.sendMoveMessage(av.getWorldLocation());
	}
}


