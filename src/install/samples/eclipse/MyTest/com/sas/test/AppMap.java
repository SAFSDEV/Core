package com.sas.test;
import org.safs.model.Component;

/** A JSAFS AppMap reference for SAFS App Map: './Datapool/App.map' */
public final class AppMap {

    /** No use for a default constructor. */
    private AppMap(){}

    /** The Names of Constants and child Component objects. */
    public static class ApplicationConstants {

        /** No use for a default constructor. */
        private ApplicationConstants(){}

        public static final Component ApplicationConstants = new Component("ApplicationConstants");
        public static final Component userid = new Component(ApplicationConstants, "userid");
        public static final Component URL = new Component(ApplicationConstants, "URL");
    }

    /** The Names of Constants and child Component objects. */
    public static class AnotherWin {

        /** No use for a default constructor. */
        private AnotherWin(){}

        public static final Component AnotherWin = new Component("AnotherWin");
        public static final Component SomeComp1 = new Component(AnotherWin, "SomeComp1");
        public static final Component AnotherComp2 = new Component(AnotherWin, "AnotherComp2");
    }

    /** The Names of Constants and child Component objects. */
    public static class MainWin {

        /** No use for a default constructor. */
        private MainWin(){}

        public static final Component MainWin = new Component("MainWin");
        public static final Component AnyComp1 = new Component(MainWin, "AnyComp1");
        public static final Component AnotherComp2 = new Component(MainWin, "AnotherComp2");
    }

}
