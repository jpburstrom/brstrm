//Fix for PatternConductor
//Makes it possible to sync to another clock


+PatternConductor {
    play { |clock|
        Routine.run({ this.prPlay }, 64, clock ? TempoClock.default, quant)
    }
}



