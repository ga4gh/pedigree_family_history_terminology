# KIN - The Family History Relations Ontology

__IMPORTANT NOTE: The layout and contents of this repository are still under discussion and the artifacts contained in it are in pre-alpha stage.__

## Availability

The latest version of the ontology can always be found at: [http://purl.org/ga4gh/kin.owl](http://purl.org/ga4gh/kin.owl).

## What is KIN?

KIN is a family relations ontology developed as part of the [Global Alliance for Genomics and Health Pedigree Standard project](https://github.com/GA4GH-Pedigree-Standard). It allows using an OWL reasoner to automatically validate a family history graph and infer new relations.

## Project Structure

The project contains two main parts: 
 - The OWL ontology file, written in OWL functional notation, available in the `src\main\resources` folder.
 - The unit test cases, written in Java and using the JFact reasoner. The main test case is available in `src\test\java\org\ga4gh\rel\OntologyTest.java`.

## Ontology Design

The KIN ontology contains mostly object properties that represent the family relations in a pedigree. No individuals are included in the core ontology and when individuals are required, to implement a test, for example, these are created programmatically and discarded after the test has finished.

## Contributing

The main requirement to run the project locally is to have Maven installed.

The easiest way to contribute is to clone the project, create a new branch and work on either the OWL file of the test cases. The OWL file can be modified using Protégé or any text editor. The test cases are written in Java.

When you are finished making changes you can run the tests using `mvn test` and make sure everything is working properly. Once you are satisfied with your changes please submit a pull request for review.
