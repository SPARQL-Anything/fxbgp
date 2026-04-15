package io.github.sparqlanything.fxbgp;

import io.github.sparqlanything.fxbgp.csv.FXCSVModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FXCSVModelTest {
	private FXCSVModel FXM;

	@Before
	public void before(){
		FXM = FXCSVModel.getFXCSVModel();
	}
}
