/*

SynthDef("PV_LocalMax", { | in=0, out=0, bufnum=0,
	// Threshold: 0 - 50
	threshold=25,
	source_level = 0, fx_level = 1 |
	var chain, source;
	source = In.ar(in);
	chain = FFT(bufnum, source);
	chain = PV_LocalMax(chain,  threshold);
	Out.ar(out, Mix.ar([IFFT(chain) * fx_level, source * source_level]))
});
*/