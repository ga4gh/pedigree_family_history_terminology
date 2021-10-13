package org.ga4gh.rel;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main class used to define ontology tests.
 */
class OntologyTest extends AbstractOntologyTest {

	/**
	 * Tests that the ontology is in the DL profile.
	 */
	@Test
	public void testInDl() {
		OWL2DLProfile dlp = new OWL2DLProfile();
		OWLProfileReport report = dlp.checkOntology(fhOntology);

		if (!report.isInProfile()) {
			System.out.println("The ontology is not in the DL profile and has the following violations:");
			for (OWLProfileViolation violation : report.getViolations()) {
				System.out.println(violation);
			}
		}

		assertTrue(report.isInProfile());
	}

	/**
	 * This test checks that the ontology is inconsistent when an invalid family graph is created. In this case, the
	 * following graph is created:
	 *
	 * Homer -> biological parent -> Bart
	 * Bart -> biological parent -> Homer
	 *
	 * The reasoner should indicate that the ontology is inconsistent because a person cannot be the biological parent and
	 * biological child of another.
	 *
	 */
	@Test
	public void testInvalidRelation() {
		try {
			// Add individuals
			OWLClass person = getNamedClass("KIN_998");
			OWLNamedIndividual bart = getNamedIndividual("Bart");
			OWLNamedIndividual homer = getNamedIndividual("Homer");

			Set<OWLAxiom> axioms = new HashSet<>();
			axioms.add(dataFactory.getOWLClassAssertionAxiom(person, bart));
			axioms.add(dataFactory.getOWLClassAssertionAxiom(person, homer));

			OWLObjectProperty biologicalParent = getNamedObjectProperty("KIN_003");
			axioms.add(dataFactory.getOWLObjectPropertyAssertionAxiom(biologicalParent, homer, bart));

			OWLObjectProperty biologicalChild = getNamedObjectProperty("KIN_032");
			axioms.add(dataFactory.getOWLObjectPropertyAssertionAxiom(biologicalChild, homer, bart));

			manager.addAxioms(fhOntology, axioms.stream());

			// Flush, classify and check consistency
			reasoner.flush();
			reasoner.precomputeInferences();
			assertFalse(reasoner.isConsistent());
		} catch (Error e) {
			e.printStackTrace();
			fail();
		} finally {
			// Always dispose of reasoner
			reasoner.dispose();
		}
	}

	/**
	 * This test checks that the reasoner infers the grandparent relation. The following graph is created:
	 *
	 * Homer -> biological parent -> Bart
	 * Abe -> biological parent -> Homer
	 *
	 * The test then checks for the presence of the following inferred edge:
	 *
	 * Abe -> grandparent -> Bart
	 *
	 */
	@Test
	public void testGrandparentInference() {
		try {
			// Add individuals
			OWLClass person = getNamedClass("KIN_998");
			OWLNamedIndividual bart = getNamedIndividual("Bart");
			OWLNamedIndividual homer = getNamedIndividual("Homer");
			OWLNamedIndividual abe = getNamedIndividual("Abe");

			Set<OWLAxiom> axioms = new HashSet<>();
			axioms.add(dataFactory.getOWLClassAssertionAxiom(person, bart));
			axioms.add(dataFactory.getOWLClassAssertionAxiom(person, homer));
			axioms.add(dataFactory.getOWLClassAssertionAxiom(person, abe));

			OWLObjectProperty biologicalParent = getNamedObjectProperty("KIN_003");
			axioms.add(dataFactory.getOWLObjectPropertyAssertionAxiom(biologicalParent, homer, bart));
			axioms.add(dataFactory.getOWLObjectPropertyAssertionAxiom(biologicalParent, abe, homer));

			manager.addAxioms(fhOntology, axioms.stream());

			// Flush, classify and check consistency
			reasoner.flush();
			reasoner.precomputeInferences();
			assertTrue(reasoner.isConsistent());

			// Get instances of people
			NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(person, true);
			abe = getNamedIndividual("Abe", individuals);
			OWLObjectProperty op = getNamedObjectProperty("KIN_017");
			NodeSet<OWLNamedIndividual> values = getObjectPropertyValues(abe, op);

			// Verify that the reasoner has inferred the grandparent relation between Bart and Abe
			assertTrue(containsNamedIndividual(values, "Bart"));
		} catch (Error e) {
			e.printStackTrace();
			fail();
		} finally {
			// Always dispose of reasoner
			reasoner.dispose();
		}
	}

}
