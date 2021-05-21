package org.ga4gh.rel;

import org.junit.jupiter.api.BeforeEach;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract parent of all ontology tests.
 */
public abstract class AbstractOntologyTest {

  public static final IRI FH_IRI = IRI.create("http://purl.org/ga4gh/rel.owl#");

  protected OWLOntologyManager manager;
  protected OWLOntology fhOntology;
  protected OWLDataFactory dataFactory;
  protected OWLReasonerFactory reasonerFactory = null;
  protected OWLReasoner reasoner;

  @BeforeEach
  public void setUp() throws OWLOntologyCreationException, IOException {
    this.reasonerFactory = new JFactFactory();
    OntologyTest app = new OntologyTest();
    this.manager = OWLManager.createOWLOntologyManager();
    this.dataFactory = manager.getOWLDataFactory();
    try (InputStream is = app.getFileFromResourceAsStream("rel.owl")) {
      this.fhOntology = manager.loadOntologyFromOntologyDocument(is);
    }
    reasoner = reasonerFactory.createReasoner(fhOntology);
  }

  protected InputStream getFileFromResourceAsStream(String fileName) {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream(fileName);

    if (inputStream == null) {
      throw new IllegalArgumentException("File not found: " + fileName);
    } else {
      return inputStream;
    }
  }

  protected OWLClass getNamedClass(String id) {
    return dataFactory.getOWLClass(IRI.create(FH_IRI + id));
  }

  protected OWLObjectProperty getNamedObjectProperty(String id) {
    return dataFactory.getOWLObjectProperty(IRI.create(FH_IRI + id));
  }

  protected OWLNamedIndividual getNamedIndividual(String id) {
    return dataFactory.getOWLNamedIndividual(IRI.create(FH_IRI + id));
  }

  protected boolean containsNamedIndividual(NodeSet<OWLNamedIndividual> values, String individualId) {
    return !values.entities()
      .filter(i -> i.getIRI().getFragment().equals(individualId))
      .collect(Collectors.toSet())
      .isEmpty();
  }

  protected NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual i, OWLObjectProperty op) {
    return reasoner.getObjectPropertyValues(i, op);
  }

  protected OWLNamedIndividual getNamedIndividual(String id, NodeSet<OWLNamedIndividual> individuals) {
    Set<OWLNamedIndividual> res =
      individuals.entities().filter(i -> i.getIRI().getFragment().equals(id)).collect(Collectors.toSet());
    if (res.isEmpty()) {
      throw new RuntimeException("Could not find individual " + id);
    }
    return res.iterator().next();
  }

  protected void printAllForClass(OWLClass c) {
    NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, true);
    instances
      .entities()
      .forEach(i -> {
        System.out.println(i.getIRI().getFragment() + "\tinstance of\t" + c.getIRI().getFragment());
        fhOntology.objectPropertiesInSignature()
          .filter( op -> !op.getIRI().getFragment().equals("topObjectProperty"))
          .forEach(op -> {
            NodeSet<OWLNamedIndividual> petValuesNodeSet = reasoner.getObjectPropertyValues(i, op);
            petValuesNodeSet.entities()
              .forEach(value -> {
                System.out.println(i.getIRI().getFragment() + "\t"
                  + op.getIRI().getFragment() + "\t"
                  + value.getIRI().getFragment());
              });
          });
      });
  }
}
