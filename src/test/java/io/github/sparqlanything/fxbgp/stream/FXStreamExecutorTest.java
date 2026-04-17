package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.BaseFacadeXGraphBuilder;
import io.github.sparqlanything.model.FacadeXGraphBuilder;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.mem.DatasetGraphInMemory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private Integer parallelThreshold = null;

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
        return properties(blankNodes, false, true);
    }

    protected final Properties propertiesEventsFilteringFalse(boolean blankNodes){
        return properties(blankNodes, false, true);
    }
    protected final Properties properties() {
        return properties(true, false,true);
    }
    protected final Properties propertiesNullValueTrue() {
        return properties(true, true, true);
    }

    protected final Properties properties(boolean blankNodes, boolean nullValues, boolean eventsFiltering) {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), input.toString());
        if("headers".equals(flavour)){
            properties.setProperty(CSVTriplifier2.PROPERTY_HEADERS.toString(), "true");
        }
        if(this.parallelThreshold != null){
            properties.setProperty(FXBroadcastEventListener.PARALLEL_THRESHOLD_OPTION, this.parallelThreshold.toString());
        }
        properties.setProperty(FXBroadcastEventListener.EVENTS_FILTERING_OPTION.toString(), eventsFiltering ? "true" : "false");
        properties.setProperty(IRIArgument.BLANK_NODES.toString(), blankNodes ? "true" : "false");
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
        if(spl.length == 5){
            this.parallelThreshold = Integer.parseInt(spl[4]);
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

    public DatasetGraph getDatasetGraphFrom_abc(String ng, Set<Binding> abc){
        Node graph= NodeFactory.createURI(ng);
        Graph g = getGraphFrom_abc(abc);
        DatasetGraph dg2 = new DatasetGraphInMemory();
        dg2.addGraph(graph, g);
        return dg2;
    }
    public Graph getGraphFrom_abc(Set<Binding> abc){
        Graph g = GraphFactory.createGraphMem();
        for(Binding binding: abc) {
            Node subject = binding.get("a");
            Node predicate = binding.get("b");
            Node object = binding.get("c");
            g.add(subject, predicate, object);
        }
        return g;
    }

    protected void show(Graph g){
        RDFDataMgr.write(System.out, g, RDFFormat.TTL);
    }

    protected DatasetGraph getDatasetGraph(Triplifier triplifier, Properties properties){
        FacadeXGraphBuilder gb = new BaseFacadeXGraphBuilder(properties);
        try {
            triplifier.triplify(properties, gb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TriplifierHTTPException e) {
            throw new RuntimeException(e);
        }
        return gb.getDatasetGraph();
    }

    protected Set<Binding> getBindings(Properties properties){
        Set<Binding> it = null;
        try {
            it = set(executor.exec(getOpBGP(), properties));
        } catch (NotATreeException e) {
            throw new RuntimeException(e);
        }
        return it;
    }

    protected ResultSet getResultSet(Properties properties){
        try {
            return ResultSetFactory.create(executor.exec(getOpBGP(), properties), getVars());
        } catch (NotATreeException e) {
            throw new RuntimeException(e);
        }
    }

    protected List<String> getVars(){
        final List<String> vars = new ArrayList<>();
        getBGP().getList().forEach(
                t -> {
                    Arrays.stream(new Node[]{t.getSubject(), t.getPredicate(), t.getObject()}).iterator().forEachRemaining(
                            n-> {
                                    if(n.isVariable())vars.add(n.getName());
                            }
                    );
                }
        );
        return vars;
    }

    protected Query getSelectQuery(){
        BasicPattern bp = getBGP();
        Query query = QueryFactory.make();
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        ElementTriplesBlock block = new ElementTriplesBlock();
        bp.getList().forEach(block::addTriple);
        query.setQueryPattern(block);
        L.debug("{}", query);
        return query;
    }

    
    /**
     * Warning! This method assumes a lot of things...
     * - bgp must be all.easybgp we expect bindings to be a b c...
     * - for example, call with method name test1_json_all_...
     * @param triplifier
     */
    public void testABCEquals(Triplifier triplifier, Properties properties) {
        DatasetGraph dg1 = getDatasetGraph(triplifier, properties);
        long size1 = dg1.getDefaultGraph().size();
        Set<Binding> it = getBindings(properties);
        Graph gg = getGraphFrom_abc(it);

        L.info("old {} vs new {}", size1, gg.size());
        L.info("OLD \n=======\n{}\n=======\n", makeString(dg1.getDefaultGraph()));
        L.info("NEW \n=======\n{}\n=======\n", makeString(gg));
        Iterator<Triple> i1 = dg1.getDefaultGraph().find();
        while(i1.hasNext()){
            Triple t = i1.next();
            if(!gg.contains(t)){
                L.error("new does not contain old: {}", t);
            }
        }
        Iterator<Triple> i2 = gg.find();
        while(i2.hasNext()){
            Triple t = i2.next();
            if(!dg1.getDefaultGraph().contains(t)){
                L.error("old does not contain new: {}", t);
            }
        }

        Assert.assertEquals(size1, gg.size());

        i1 = dg1.getDefaultGraph().find();
        while(i1.hasNext()){
            Assert.assertTrue(gg.contains(i1.next()));
        }

        i2 = gg.find();
        while(i2.hasNext()){
            Assert.assertTrue(dg1.getDefaultGraph().contains(i2.next()));
        }
    }

    protected void testSelectStarEquals(Triplifier triplifier, Properties properties) {
        DatasetGraph dg1 = getDatasetGraph(triplifier, properties);
        L.debug("Dataset graph size: {}", dg1.getDefaultGraph().size());
        QueryExecution qe = QueryExecutionFactory.create(getSelectQuery(), dg1);
        ResultSet rs1 = qe.execSelect();
        Set<Map<String,RDFNode>> qs1 = new HashSet<>();
        rs1.forEachRemaining(qs->{
            Map<String, RDFNode> map = new HashMap<>();
            qs.varNames().forEachRemaining(v ->map.put(v, qs.get(v)));
            qs1.add(map);
        });
//        qs1.forEach(System.out::println);
        System.out.println("---");
        Set<Map<String,RDFNode>> qs2 = new HashSet<>();
        getResultSet(properties).forEachRemaining(qs->{
            Map<String, RDFNode> map = new HashMap<>();
            qs.varNames().forEachRemaining(v->map.put(v, qs.get(v)));
            qs2.add(map);
        });
//        qs2.forEach(System.out::println);
        L.debug("Select equals? {} vs {}", qs1.size(),qs2.size());
        L.debug("Select equals? {}", qs1.equals(qs2));
//        L.error("Select equals? {}", qs1.equals(qs2));
        qs2.forEach(q->{
            if(!qs1.contains(q)){
                L.error("old does not contain new: {}", q);
            }
        });
        qs1.forEach(q->{
            if(!qs2.contains(q)){
                L.error("new does not contain old: {}", q);
            }
        });
        Assert.assertEquals(qs1,qs2);
    }

    protected String makeString(Graph g){
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, true, utf8)) {
            RDFDataMgr.write(ps, g, RDFFormat.TTL);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        try {
            return baos.toString(utf8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
