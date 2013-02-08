+ SoftStepLed {

    blink { arg color=0, len=0.1;
        this.off;
        fork {
            out.control(3, colors[color][button], 1);
            len.wait;
            this.off;
        }
    }

}
