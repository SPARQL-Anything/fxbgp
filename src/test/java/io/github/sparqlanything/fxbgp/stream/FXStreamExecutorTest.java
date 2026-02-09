package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

abstract class FXStreamExecutorTest extends BGPTestUtils {
    FXStreamExecutor executor;
    @Rule
    public TestName testName = new TestName();
    private URL input;
    private BasicPattern bp;
    private String flavour;

    @Before
    public void before(){
        executor = new FXStreamExecutor();
    }

    public OpBGP getOpBGP(){
        return new OpBGP(bp);
    }

    public BasicPattern getBGP(){
        return bp;
    }

    @Ignore
    @Test
    public void writers(){
        ResultSetWriterRegistry.init();
        for(Lang l :ResultSetWriterRegistry.registered()){
            L.info("registered: {}", l);
        }
    }

    protected final void show(Iterator<Binding> qit){
        while(qit.hasNext()){
            L.info(" ---- ");
            Binding qs = qit.next();
            Iterator<Var> it = qs.vars();
            while(it.hasNext()){
                String var = it.next().getVarName();
                L.info("Solution: {} -> {}", var, qs.get(var));
            }
        }
    }

    protected final Properties properties(boolean blankNodes){
        return properties(blankNodes, false);
    }
    protected final Properties properties() {
        return properties(true, false);
    }
    protected final Properties propertiesNullValueTrue() {
        return properties(true, true);
    }

    protected final Properties properties(boolean blankNodes, boolean nullValues) {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), input.toString());
        if("headers".equals(flavour)){
            properties.setProperty(CSVTriplifier2.PROPERTY_HEADERS.toString(), "true");
        }

        properties.setProperty("blank-nodes", blankNodes ? "true" : "false");
        properties.setProperty(JSONTriplifier.PROPERTY_JSONINCLUDENULLVALUES.toString(), nullValues ? "true" : "false");

        String mediaType = null;
        if(input.getPath().endsWith(".csv")){
            mediaType = "text/csv";
        }else if(input.getPath().endsWith(".json")){
            mediaType = "application/json";
        }else if(input.getPath().endsWith(".xml")){
            mediaType = "application/xml";
        }
        properties.setProperty("media-type", mediaType);
        return properties;
    }

    protected void prepare(String methodName) throws IOException {
        String[] spl = testName.getMethodName().split("_");
        String inputName = spl[0] + "." + spl[1];
        String easyBGPName = spl[2];
        if(spl.length == 4){
            this.flavour = spl[3];
        }
        this.input = getClass().getClassLoader().getResource("./stream/" + inputName);
        this.bp = readBGP("./stream/" + easyBGPName);
    }

    protected final Set<Binding> set(QueryIterator qit){
        Set<Binding> set = new HashSet<>();
        while(qit.hasNext()){
            set.add(qit.next());
        }
        return set;
    }

    protected final boolean rem(Set<Binding> qs, String[] ... var_regex_conditions){
        Set<Binding> set = new HashSet<>(qs);
        Set<String[]> unmet = new HashSet<>();
        Set<Binding> foundd = new HashSet<>();
        for(String[] var_regex : var_regex_conditions){
            boolean found = false;
            // If a query solution resolves all conditions is removed
            for(Binding q : qs){
                int successes = 0;
                for(int index = 0; index < var_regex.length; index = index + 2){
                    String var = var_regex[index];
                    String regex = var_regex[index + 1];
                    Node node = q.get(var);
                    String vvv ;
                    if(node.isURI()){
                        vvv = node.getURI().toString();
                    }else if(node.isLiteral()){
                        vvv = node.getLiteralLexicalForm().toString();
                    }else if(node.isBlank()) {
                        vvv = node.getBlankNodeLabel().toString();
                    }else{
                        throw new RuntimeException("This should not happen");
                    }
                    //L.trace("Testing {} vs {}", node, regex);
                    if(Pattern.compile(regex).matcher(vvv).find()){
                        //L.trace("Success {} vs {}", node, regex);
                        successes ++;
                    }else {
                        break;
                    }
                }
                if(successes == var_regex.length/2){
                    //L.trace("Successes {} vs {}", successes, var_regex.length/2);
                    set.remove(q);
                    found = true;
                    foundd.add(q);
                }
            }
            //
            if(!found){
                unmet.add(var_regex);
            }
        }
        L.trace("Solutions matching: {}", foundd.size());
        if(!set.isEmpty()){
            L.error("Solutions not matching anything: {}", set.size());
            L.error("Example:");
            for (Binding q : set) {
                L.error(q.toString());
                break;
            }
        }
        if(!unmet.isEmpty()){
            L.error("Matches without solutions: {}", unmet.size());
            L.error("Example:");
            for (String[] q : unmet) {
                L.error(StringUtils.join(q, ","));
                break;
            }
        }
        return set.isEmpty() && unmet.isEmpty();
    }
}
