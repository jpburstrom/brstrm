+RedAutoScale {

    //Store current min/max as inLo/inHi
    storeArgs {
        ^[lo, hi, min, max]
    }

    //
    hardReset {
        inLo = inf;
        inHi = -inf;
        this.reset
    }
}
