package io.github.sparqlanything.fxrdb;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestAbstract;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RDBAnalyserTest extends BGPTestAbstract {
	public RDBAnalyserTest() {
		super(FXRDBModel.getFXRDBModel());
	}

	@Before
	public void setup() {
		setAnalyser(new AnalyserGrounder(properties, FXM()));
	}

	@Test
	public void var_var_var(){
		add(v("a"), v("b"), v("c"));
		FXBGPAnnotation i1 = make(
			v("a"), FXRDB.ContainerEntity,
			v("b"), FXRDB.TypeProperty,
			v("c"), FXRDB.TypeTable
		);
		FXBGPAnnotation i2 = make(
			v("a"), FXRDB.ContainerTable,
			v("b"), FXRDB.TypeProperty,
			v("c"), FXRDB.Root
		);
		FXBGPAnnotation i3 = make(
			v("a"), FXRDB.ContainerEntity,
			v("b"), FXRDB.SlotColumn,
			v("c"), FXRDB.Cell
		);
		FXBGPAnnotation i4 = make(
			v("a"), FXRDB.ContainerTable,
			v("b"), FXRDB.SlotRow,
			v("c"), FXRDB.ContainerEntity
		);
		Assert.assertTrue(annotations().contains(i1));
		Assert.assertTrue(annotations().contains(i2));
		Assert.assertTrue(annotations().contains(i3));
		Assert.assertTrue(annotations().contains(i4));
		L.error("{}", annotations());
		Assert.assertEquals(4, annotations().size());
	}

	@Test
	public void var_rdftype_table(){
		add(t(v("container"), RDF.type.asNode(), xyz("table")));
		FXBGPAnnotation i1 = make(
			v("container"), FXRDB.ContainerEntity,
			RDF.type.asNode(), FXRDB.TypeProperty,
			xyz("table"), FXRDB.TypeTable
		);
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void var_rdftype_root(){
		// ?table a []
		add(v("table"), RDF.type.asNode(), u(Triplifier.FACADE_X_TYPE_ROOT));
		FXBGPAnnotation i1 = make(
			v("table"), FXRDB.ContainerTable,
			RDF.type.asNode(), FXRDB.TypeProperty,
			u(Triplifier.FACADE_X_TYPE_ROOT), FXRDB.Root
		);
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void b_cmp_b(){
		Node n = b();
		Node n2 = b();
		add(n, RDF.li(1).asNode(), n2);

		FXBGPAnnotation i1 = make(
			n, FXRDB.ContainerTable,
			RDF.li(1).asNode(), FXRDB.SlotRow,
			n2, FXRDB.ContainerEntity
		);
		L.error("{}", annotations());
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void b_var_l(){
		Node n = b();
		add(n, v("p"), l("12"));

		FXBGPAnnotation i1 = make(
			n, FXRDB.ContainerEntity,
			v("p"), FXRDB.SlotColumn,
			l("12"), FXRDB.Cell
		);
		L.error("{}", annotations());
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}


	@Test
	public void var_var_l(){
		add(v("x"), v("p"), l(12));
		FXBGPAnnotation i1 = make(
			v("x"), FXRDB.ContainerEntity,
			v("p"), FXRDB.SlotColumn,
			l(12),  FXRDB.Cell
		);
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void s_P_L__s_P_L(){
		add(v("s"), v("p1"), l(12));
		add(v("s"), v("p2"), l("a string"));

		FXBGPAnnotation i1 = make(
			v("s"), FXRDB.ContainerEntity,
			v("p1"), FXRDB.SlotColumn,
			l(12), FXRDB.Cell,
			v("p2"), FXRDB.SlotColumn,
			l("a string"), FXRDB.Cell
		);
		L.error("{}", annotations());
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void S_P_l__S_P_l(){
		add(v("s1"), v("p1"), l(12));
		add(v("s2"), v("p2"), l(12));
		FXBGPAnnotation i1 = make(
			v("s1"), FXRDB.ContainerEntity,
			v("p1"), FXRDB.SlotColumn,
			l(12), FXRDB.Cell,
			v("s2"), FXRDB.ContainerEntity,
			v("p2"), FXRDB.SlotColumn
		);
		L.error("{}", annotations());
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void S_P_v__S_P_v(){
		add(v("s1"), v("p1"), v("x"));
		add(v("s2"), v("p2"), v("x"));
		FXBGPAnnotation i1 = make(
			v("s1"), FXRDB.ContainerEntity,
			v("p1"), FXRDB.SlotColumn,
			v("x"), FXRDB.Cell,
			v("s2"), FXRDB.ContainerEntity,
			v("p2"), FXRDB.SlotColumn
		);
		FXBGPAnnotation i2 = make(
			v("s1"), FXRDB.ContainerTable,
			v("p1"), FXRDB.SlotRow,
			v("x"), FXRDB.ContainerEntity,
			v("s2"), FXRDB.ContainerTable,
			v("p2"), FXRDB.SlotRow
		);
		FXBGPAnnotation i3 = make(
			v("s1"), FXRDB.ContainerTable,
			v("p1"), FXRDB.TypeProperty,
			v("x"), FXRDB.Root,
			v("s2"), FXRDB.ContainerTable,
			v("p2"), FXRDB.TypeProperty
		);
		FXBGPAnnotation i4 = make(
			v("s1"), FXRDB.ContainerEntity,
			v("p1"), FXRDB.TypeProperty,
			v("x"), FXRDB.TypeTable,
			v("s2"), FXRDB.ContainerEntity,
			v("p2"), FXRDB.TypeProperty
		);
		L.error("{}", annotations());
		Assert.assertEquals(4, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
		Assert.assertTrue(annotations().contains(i2));
		Assert.assertTrue(annotations().contains(i3));
		Assert.assertTrue(annotations().contains(i4));
	}

	@Test
	public void S_P_V__S_P_V_same_P_V(){
		add(v("s1"), v("p1"), v("x"));
		add(v("s2"), v("p1"), v("x"));
		FXBGPAnnotation i1 = make(
			v("s1"), FXRDB.ContainerEntity,
			v("p1"), FXRDB.SlotColumn,
			v("x"), FXRDB.Cell,
			v("s2"), FXRDB.ContainerEntity
		);
		FXBGPAnnotation i2 = make(
			v("s1"), FXRDB.ContainerEntity,
			v("p1"), FXRDB.TypeProperty,
			v("x"), FXRDB.TypeTable,
			v("s2"), FXRDB.ContainerEntity
		);

		FXBGPAnnotation i3 = make(
			v("s1"), FXRDB.ContainerTable,
			v("p1"), FXRDB.TypeProperty,
			v("x"), FXRDB.Root,
			v("s2"), FXRDB.ContainerTable
		);

		FXBGPAnnotation i4 = make(
			v("s1"), FXRDB.ContainerTable,
			v("p1"), FXRDB.SlotRow,
			v("x"), FXRDB.ContainerEntity,
			v("s2"), FXRDB.ContainerTable
		);
		L.error("{}", annotations());
		Assert.assertEquals(4, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
		Assert.assertTrue(annotations().contains(i2));
		Assert.assertTrue(annotations().contains(i3));
		Assert.assertTrue(annotations().contains(i4));
	}

	@Test
	public void S_u_v__S_P_v(){
		add(v("s1"), xyz("address"), v("x"));
		add(v("s2"), v("p2"), v("x"));
		FXBGPAnnotation i1 = make(
			v("s1"), FXRDB.ContainerEntity,
			xyz("address"), FXRDB.SlotColumn,
			v("x"), FXRDB.Cell,
			v("s2"), FXRDB.ContainerEntity,
			v("p2"), FXRDB.SlotColumn
		);
		L.error("{}", annotations());
		Assert.assertEquals(1, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
	}

	@Test
	public void u_P_v(){
		add(u("http://www.example.org/tablename"), v("p"), v("o"));
		FXBGPAnnotation i1 = make(
			u("http://www.example.org/tablename"), FXRDB.ContainerEntity,
			v("p"), FXRDB.SlotColumn,
			v("o"), FXRDB.Cell
		);
		FXBGPAnnotation i2 = make(
			u("http://www.example.org/tablename"), FXRDB.ContainerEntity,
			v("p"), FXRDB.TypeProperty,
			v("o"), FXRDB.TypeTable
		);

		FXBGPAnnotation i3 = make(
			u("http://www.example.org/tablename"), FXRDB.ContainerTable,
			v("p"), FXRDB.SlotRow,
			v("o"), FXRDB.ContainerEntity
		);

		FXBGPAnnotation i4 = make(
			u("http://www.example.org/tablename"), FXRDB.ContainerTable,
			v("p"), FXRDB.TypeProperty,
			v("o"), FXRDB.Root
		);
		L.error("{}", annotations());
		Assert.assertEquals(4, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
		Assert.assertTrue(annotations().contains(i2));
		Assert.assertTrue(annotations().contains(i3));
		Assert.assertTrue(annotations().contains(i4));
	}

	@Test
	public void B_P_b__b_P_O() {
		Node t = b();
		Node b = b();
		add(t, v("p1"), b);
		add(b, v("p2"), v("o"));
		L.error("{}", bp());
		FXBGPAnnotation i1 = make(
			t, FXRDB.ContainerTable,
			v("p1"), FXRDB.SlotRow,
			b, FXRDB.ContainerEntity,
			v("p2"), FXRDB.SlotColumn,
			v("o"), FXRDB.Cell
		);

		FXBGPAnnotation i2 = make(
			t, FXRDB.ContainerTable,
			v("p1"), FXRDB.SlotRow,
			b, FXRDB.ContainerEntity,
			v("p2"), FXRDB.TypeProperty,
			v("o"), FXRDB.TypeTable
		);
		L.error("{}", annotations());
		Assert.assertEquals(2, annotations().size());
		Assert.assertTrue(annotations().contains(i1));
		Assert.assertTrue(annotations().contains(i2));
	}

}
