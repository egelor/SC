
/* 
Rewriting UniqueSynth to use ServerReady for booting synth and thereby ensure SynthDefs and Buffers are loaded
before it starts.
*/

AbstractUniqueServerObject : UniqueObject {
	var <server;
	*makeKey { | key, target |
		^this.mainKey ++ [target.asTarget.server, key.asKey];
	}
	
	init { | target |
		server = target.asTarget.server;
	}

	*onServer { | server |
		var path;
		path = this.mainKey.add(server ? Server.default);
		if (objects.atPath(path).isNil) { ^[] };
		^objects.leaves(path);
	}	
}

UniqueSynth : AbstractUniqueServerObject {
	*mainKey { ^[UniqueSynth] } // subclasses store instances under UniqueSynth

	*new { | key, defName, args, target, addAction=\addToHead ... moreArgs |
		^super.new(key, target.asTarget, defName ?? { key.asSymbol }, args, addAction, *moreArgs);
	}

	init { | target, defName ... moreArgs |
		super.init(target);
		ServerPrep(server).addSynth({ this.makeObject(target, defName, *moreArgs); });
		if (server.serverRunning.not) { server.boot };
	}

	makeObject { | target, defName, args, addAction ... otherArgs |
		this.prMakeObject(target, defName, args, addAction, *otherArgs);
		this.registerObject;
	}

	prMakeObject { | target, defName, args, addAction |
		object = Synth(defName, args, target, addAction);
	}
 
	registerObject {
		object addDependant: { | me, what |
			switch (what, 
				\n_go, {
					this.synthStarted
				},
				\n_end, {
					this.remove;
					object.releaseDependants; // clean up synth's dependants
				}
			);
		};
		object.register;
	}

	synthStarted {
		postf("% : %\n", this.class.name, thisMethod.name); 
		object.isPlaying = true; // set status to playing when missed because started on boot time
		NotificationCenter.notify(this, \synthStarted, this);
	}

	synth { ^object }						// synonym
	isPlaying { ^object.isPlaying; }

	// Synchronization with start / stop events: 
	onStart { | func |
		if (this.isPlaying) {
			func.(this);	
		}{
			NotificationCenter.registerOneShot(this, \synthStarted, UniqueID.next, { func.(this) });
		}
	}
	onEnd { | func | this.onClose(func) }	// synonym
	
	rsync { | func, clock |
		var routine;
		ServerPrep(server).addRoutine({
			routine = { func.(object, this) }.fork(clock ? AppClock);
		});
		this.onEnd({
			routine.stop;	
		});
	}

	rsyncs { | func | this.rsync(func, SystemClock) }
	rsynca { | func | this.rsync(func, AppClock) }

	dur2 { | dtime = 1, fadeOut = 3.2, message |
		"dur2 test: received".postln;
		this.onStart({ 
			"dur2 test --- how many times is this received?".postln;
			{ 
				"this is after dtime".postln;
				this.perform(message ? \releaseDebug, fadeOut);
//				this.releaseDebug(fadeOut);
			}.defer(dtime);
		});
	}
	
	releaseDebug { | dtime |
		"UniqueSynth releaseDebug performed".postln;
		if (this.isPlaying ) { object.release(dtime) } 
	}
	
	dur { | dtime = 1, fadeOut = 0.2, message |
		this.onStart({
			postf("DUR ONSTART DEBUG %, : dtime: %, fadeout:% \n", thisMethod.name, dtime, fadeOut).postln;

//			{ 
//				"Dur sending release".postln;
//				this.perform(message ? \release, fadeOut)
//			}.defer(dtime ? inf)
		})

		}

	free { if (this.isPlaying ) { object.free } }	// safe free: only runs if not already freed
	releaseSynth { | dtime = 0.02 |
		postf("%: %, : dtime%\n", this.class.name, thisMethod.name, dtime).postln;
		if (this.isPlaying ) { object.release(dtime) } 
	}	
}
