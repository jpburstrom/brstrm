+String {

    newTextWindow { arg title="Untitled", makeListener=false;
        var path, dir = Library.at(\brstrm, \tmpDocDir);
        dir = dir ?? "/tmp";
        path = dir +/+ title ++ ".scd";
        while ( { File.exists(path) } ) { path = PathName(path).realNextName } ;

        path.postln;

        File.use(path, "w", { |f|
            f.write(this)
        });

        path.openDocument;

    }

}
