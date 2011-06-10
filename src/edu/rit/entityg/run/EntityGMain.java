package edu.rit.entityg.run;

import edu.rit.entityg.AbstractEntityG;
import edu.rit.entityg.CSVEntityG;
import edu.rit.entityg.configure.EntityGConfiguration;

/**
 * Simple Main file to test run configurations of EntityG.
 * @date May 24, 2011
 * @author Eric Kisner
 *
 */
public class EntityGMain {

    public static void main( String[] args ) {

        try {
            CSVEntityG main = new CSVEntityG();
            main.set_use_tool_tip( true );
            main.set_center_node_column_name( "President" );
            main.set_center_node_column_number( "1" );
            main.set_column_to_name_mapping( "Presidency,President,Took office,Left office,Party,Home State" );
            main.set_file_name( "USPresident Wikipedia URLs Thmbs HS.csv" );
            main.set_information_node_column_numbers( "0,4,5" );
            main.connectToDataSource();
            main.start();
        } catch( Exception e ) {
            System.err.println( e.getMessage() );
            e.printStackTrace();
        }

        //Test standalone EntityG - uses "args" + entityg config file
//        try {
//            EntityGConfiguration main = new EntityGConfiguration( args );
//            main.startEntityG();
//        } catch( Exception e ) {
//            System.err.println( e.getMessage() );
//        }

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
