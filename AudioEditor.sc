//OSX only...

AudioEditor {

    classvar <>primary; //= "/Applications/TwistedWave.app";
    classvar <editors; // = IdentityDictionary();

    *init {
        primary = primary ?? "/Applications/TwistedWave.app";
        editors = editors ?? IdentityDictionary();
    }

    *addEditor { arg key, app;
        editors[key] = app;
    }

    *open { arg sf, editor;

        primary ?? { this.init };

        editor = editors.at(editor) ?? primary;

        if (File.exists(sf)) {
            // ("open " ++ sf ++ " -a " ++ editor).postln;
            ("open \"" ++ sf ++ "\" -a \"" ++ editor ++ "\"").unixCmd(postOutput:false)
            ^true
        } {
            ^false
        }
    }


}

+Buffer {
    edit { arg app;
        AudioEditor.open(this.path, app)
    }
}

+SoundFile {
    edit { arg app;
        AudioEditor.open(this.path, app)
    }
}

+Sample {
    edit { arg app;
        AudioEditor.open(this.soundFile.path, app)
    }
}

+String {
    pathEdit { arg app;
        AudioEditor.open(this, app)
    }
}