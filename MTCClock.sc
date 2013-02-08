//MTCClock
//Based on AudioMulchClock by f.olofsson & j.liljedahl 2010-2011
//todo:	legato in pbind not working properly - why? (is this still the case??)

//Receiving MTC via midi
SMPTEResponder : MIDIResponder {
	classvar <norinit = false,<nonr;

	*new { arg function, src, chan, num, veloc, install=true,swallowEvent=false;
		^super.new.function_(function)
			.matchEvent_(MIDIEvent(nil, src, chan))
			.init(install)
	}
	*initialized { ^norinit }
	*responders { ^nonr }
	*init {
		if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
		norinit = true;
		nonr = [];
		MIDIIn.smpte = { arg src, index, data;
			nonr.any({ arg r;
				r.respond(src, index, data)
			});
		}
	}
	*add { arg resp;
		nonr = nonr.add(resp);
	}
	*remove { arg resp;
		nonr.remove(resp);
	}
}


MTCClock {
	var	<running = false, <tick = 0, <>shift = 0, <>beatsPerBar= 4, <>permanent= false,
		<tempo = 1, <>startAction = nil, <>stopAction = nil, <currentTime, <>fps, pos,
		queue, pulse;
		
	classvar defaultClock = nil;
	*new { arg framerate=24;
		^super.new.initMTCClock(framerate);
	}
	*default { 
		^defaultClock ?? {
			(defaultClock = MTCClock.new).permanent = true;
		};
	}
	initMTCClock { arg framerate=24;
		
		queue= PriorityQueue.new;
		currentTime = Array.fill(7);
		pos = Array.fill(8, 0);
		fps = framerate;
		
		pulse= SMPTEResponder({|src, index, data|
			//[src, index, data].postln
			this.doPulse(src, index, data);
		}, nil, nil);
		
		CmdPeriod.doOnce {this.clear};
	}
	doPulse {|src, index, d|
		var time;
		
//pos.postln;
		(index < 7).if ({	
			pos[7-index] = d;
		}, {
			//MSF 
			//Hour 
			pos[7-index] = (d & 16) + pos[7-index+1];
			
			(0..3).do { arg i;
				//pos[i].postln;
				currentTime[i] = pos[i*2] + pos[(i*2)+1]; 
			};
			
			//Set framerate
			currentTime[5] = fps;
			
			//We're using a set framerate instead
			/*
			currentTime[6] = false;
			#[25,30,30].do { arg item, i; 
				(d & (1 << (i + 5)) != 0).if {
					currentTime[5] = item;
					(i == 2).if { currentTime[6] = true; }
				}
			};
			*/
			
			//Framecount
			currentTime[4] = (((((currentTime[0] * 60) + currentTime[1]) * 60) + currentTime[2]) * currentTime[5]) + currentTime[3];
			
		//currentTime.postln;
			tick = currentTime[4];
			//framerate = currentTime[5];
	
			while({time = queue.topPriority; time.notNil and:{time.floor<=tick}}, { 
				this.doSched(time-tick, queue.pop, currentTime[5]);
			});
			running= true;
			//currentTime[0..3].postln;

		});
		


		
	}
	doSched {|ofs, item, tickdur|
		var delta;
		//("doSched tickdur: "++tickdur++" ofs: "++ofs++" tick: "++tick).postln;
		SystemClock.sched(ofs * tickdur, {
			delta = item.awake(tick, Main.elapsedTime, this);
			if(delta.isNumber, {
				this.sched(delta+(ofs/currentTime[5]), item);
			});
			nil;
		});
	}
	play {|task, quant= 1|
		this.schedAbs(this.nextTimeOnGrid(quant), task);
	}
	beatDur {
		^1/tempo;
	}
	beats {
		^tick/currentTime[5];
	}
	sched {|delta, item|
//currentTime.postln;
		queue.put(tick+(delta*fps), item);
//queue.isEmpty.postln;
	}
	
	fromFPS {|i|
		^i/fps;
	}
	
	
	schedAbs {|tick, item|
		queue.put(tick, item);
	}
	nextTimeOnGrid {|quant= 1, phase= 0|
		if(quant.isNumber.not, {
			quant= quant.quant;
		});
		if(quant==0, {^tick+(phase*fps)});
		if(tick==0, {^phase*fps});
		^tick+((fps*quant)-(tick%(fps*quant)))+(phase%quant*fps);
	}
	
	clear {
//(this.class.name++": clear").postln;
		queue.array.pairsDo{|time, item| item.removedFromScheduler};
		queue.clear;
		if(permanent.not, {
			pulse.remove;
		},{
			CmdPeriod.doOnce {this.clear};
		});
		running= false;
	}
}
