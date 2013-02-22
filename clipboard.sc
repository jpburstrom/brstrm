// OSX-specific, could be
+Object {
    ccopy {
        Platform.case(
            \osx, { ("echo \"" ++ this.asCode.escapeChar($") ++ "\" | pbcopy").unixCmd(postOutput:false) },
            \linux, { ("echo \"" ++ this.asCode.escapeChar($") ++ "\" | xclip --selection  clipboard").unixCmd(postOutput:false) },
            \windows, { "Clipboard not supported".postln }
        );
    }
}
