// Adding some extra QKey enums

QKey2 {
    classvar
    <escape = 		16r01000000,
    <tab = 			16r01000001,
    <backtab = 		16r01000002,
    <backspace =		16r01000003,
    <return =		16r01000004,
    <enter = 		16r01000005,
    <insert =		16r01000006,
    <delete =		16r01000007,
    <pause = 		16r01000008,
    <print = 		16r01000009,
    <sysReq =		16r0100000a,
    <clear = 		16r0100000b,
    <home = 			16r01000010,
    <end = 			16r01000011,
    <left = 			16r01000012,
    <up = 			16r01000013,
    <right = 		16r01000014,
    <down = 			16r01000015,
    <pageUp = 		16r01000016,
    <pageDown = 		16r01000017,
    <shift = 		16r01000020,
    <control = 		16r01000021,
    <meta = 			16r01000022,
    <alt = 			16r01000023,
    <altGr = 		16r01001103,
    <capsLock = 		16r01000024,
    <numLock = 		16r01000025,
    <scrollLock = 	16r01000026,
    <f1 = 			16r01000030,
    <f2 = 			16r01000031,
    <f3 = 			16r01000032,
    <f4 = 			16r01000033,
    <f5 = 			16r01000034,
    <f6 = 			16r01000035,
    <f7 = 			16r01000036,
    <f8 = 			16r01000037,
    <f9 = 			16r01000038,
    <f10 = 			16r01000039,
    <f11 = 			16r0100003a,
    <f12 = 			16r0100003b;
}

+ QKey {

    *doesNotUnderstand {|... msg|
        if (QKey2.respondsTo(msg[0])) {
            ^QKey2.perform(msg[0]);
        } {
            DoesNotUnderstandError(this, msg[0], msg[1..]).throw;
        }
    }
}
