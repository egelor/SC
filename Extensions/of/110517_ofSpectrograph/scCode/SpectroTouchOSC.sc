SpectroTouchOSC {

	*makeResponders {
	OSCresponder.new(nil, "/numTeeth", { arg time, resp, msg; 
				\pv_rectcomb_0.set(\numTeeth, msg[1].round);
				msg[1].round.postln;	
				} ).add;
	OSCresponder.new(nil,"/spectroRed",{|time, resp, msg|OF.float("spectroRed", msg[1]);}).add;
	OSCresponder.new(nil,"/spectroGreen",{|time, resp, msg| OF.float("spectroGreen",msg[1]);}).add;
	OSCresponder.new(nil,"/spectroBlue",{|time, resp, msg| OF.float("spectroBlue", msg[1]);}).add;
				
	}	
}