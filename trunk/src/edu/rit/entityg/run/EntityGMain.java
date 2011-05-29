package edu.rit.entityg.run;

import edu.rit.entityg.configure.EntityGConfiguration;
import edu.rit.entityg.utils.ExceptionUtils;

/**
 * @date May 24, 2011
 * @author Eric Kisner
 */
public class EntityGMain {

    public static void main( String[] args ) {
        //Test standalone EntityG - uses "args" + entityg config file
        try {
            EntityGConfiguration main = new EntityGConfiguration( args );
            main.startEntityG();
        } catch( Exception e ) {
            ExceptionUtils.handleException( e );
        }

        //Test callable EntityG - uses entityg config file only
//        try {
//            EntityGConfiguration main = new EntityGConfiguration();
//            main.startEntityG();
//        } catch( Exception e ) {
//            System.err.println( e );
//            e.printStackTrace();
//        }
//
//        try {
//            EntityGConfiguration main = new EntityGConfiguration( "entityg.ini" );
//            main.startEntityG();
//        } catch( Exception e ) {
//            System.err.println( e );
//            e.printStackTrace();
//        }
    }
}
