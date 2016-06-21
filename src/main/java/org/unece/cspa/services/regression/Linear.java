package org.unece.cspa.services.regression;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.Vector;
import org.unece.cspa.services.regression.objects.Model;
import org.unece.cspa.services.regression.objects.Point;
import org.unece.cspa.services.regression.objects.Points;

/**
 * Service resource (exposed at "linear" path)
 */
 @Path("linear")
public class Linear {

	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Model regression(Points points) {

		List<Double> xCoordinates = new ArrayList<Double>();
		List<Double> yCoordinates = new ArrayList<Double>();

		// Transform list of points in two vectors x and y
		for (Point point : points.getPoint()) {
			xCoordinates.add(point.getX());
			yCoordinates.add(point.getY());
		}
		DoubleVector xVector = new DoubleArrayVector(xCoordinates);
		DoubleVector yVector = new DoubleArrayVector(yCoordinates);

		// Start the R engine
		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		RenjinScriptEngine engine = factory.getScriptEngine();

		// Execute regression in R
		engine.put("x", xVector);
		engine.put("y", yVector);
		ListVector rModel = null;
		try {
			rModel = (ListVector)engine.eval("lm(y ~ x)");
		} catch (ScriptException e) {
			return null;
		}

		// Send back coefficients extracted from result model
		Vector coefficients = rModel.getElementAsVector("coefficients");
		Model model = new Model();
		model.setIntercept(coefficients.getElementAsDouble(0));
		model.setSlope(coefficients.getElementAsDouble(1));
		return model;
	}
 }

