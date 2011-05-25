package edu.rit.entityg.configure;

import edu.rit.entityg.AbstractEntityG;
import edu.rit.entityg.DatabaseEntityG;
import edu.rit.entityg.exceptions.InvalidIniException;
import edu.rit.entityg.utils.ExceptionUtils;
import java.lang.reflect.Method;

import static edu.rit.entityg.configure.EntityGOptions.*;

/**
 * @date May 24, 2011
 * @author Eric Kisner
 */
public class EntityGConfiguration {

    private EntityGIniFile entitygINI;
    private EntityGCommandLine entitygCL;
    private AbstractEntityG entityG;
    private boolean emptyArgs = true;

    public EntityGConfiguration() throws InvalidIniException {
        doShit( new String[]{}, null );
    }

    public EntityGConfiguration( String[] args ) throws InvalidIniException {
        doShit( args, null );
    }

    public EntityGConfiguration( String iniFile ) throws InvalidIniException {
        doShit( new String[]{}, iniFile );
    }

    public void startEntityG() {
        entityG.start();
    }

    public final void doShit( String[] args, String filePath ) throws InvalidIniException {
        emptyArgs = !(args.length > 0);

        if( !emptyArgs ) {
            entitygCL = new EntityGCommandLine( args );
            if( entitygCL.optionExists( CONFIG_FILE ) ) {
                entitygINI = new EntityGIniFile( entitygCL.getOptionValue( CONFIG_FILE ), emptyArgs );
            }
        } else {
            if( filePath != null ) {
                entitygINI = new EntityGIniFile( filePath, emptyArgs );
            } else {
                entitygINI = new EntityGIniFile( emptyArgs );
            }
        }

        String dst = getValue( DATASOURCE_TYPE );

        if( dst.equalsIgnoreCase( "database" ) ) {
            entityG = new DatabaseEntityG();
            doSomeMoreShit();
            entityG.connectToDatabase();
        }
    }

    private void doSomeMoreShit() {
        try {
            for( String option : getDatabaseOptions() ) {
                Method m = getMethodFromOption( option );
                m.invoke( entityG, getValue( option ) );
            }
        } catch( Exception e ) {
            ExceptionUtils.handleException( e );
        }

        String defMaxNodeStr = getValue( DEFAULT_MAX_NODES );
        if( !defMaxNodeStr.isEmpty() ) {
            entityG.set_default_max_nodes( Integer.parseInt( defMaxNodeStr ) );
        }

        String useToolTipStr = getValue( USE_TOOL_TIP );
        if( !useToolTipStr.isEmpty() ) {
            entityG.set_use_tool_tip( Boolean.parseBoolean( useToolTipStr ) );
        }
    }

    private Method getMethodFromOption( String optionName ) {
        final String prefix = "set_";
        for( Method m : AbstractEntityG.class.getMethods() ) {
            if( (prefix + optionName).equalsIgnoreCase( m.getName() ) ) {
                return m;
            }
        }
        return null;
    }

    private String getValue( String optionName ) {
        if( !emptyArgs ) {
            if( entitygCL.optionExists( optionName ) ) {
                return entitygCL.getOptionValue( optionName );
            }
        } else {
            if( entitygINI.optionExists( optionName ) ) {
                return entitygINI.getOptionValue( optionName );
            }
        }
        return "";
    }
}
