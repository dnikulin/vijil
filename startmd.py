from glob import glob
import os

pkgs = "file index lexer model parse render report result scripts store text tools traits".split()
langs = "Java Scala".split()

for pkg in pkgs:
    for lang in langs:
        lowlang = lang.lower()
        lpath = ("src/main/%s/com/dnikulin/vijil/%s" % (lowlang, pkg))

        if os.path.exists(lpath):
            ppath = ("com.dnikulin.vijil.%s (%s)" % (pkg, lang))
            print(ppath)
            print("=" * len(ppath))
            print

            gpath = (lpath + "/*." + lowlang)
            for src in sorted(glob(gpath)):
                bname = os.path.basename(src)
                print(bname)
                print('-' * len(bname))
                print

        print

