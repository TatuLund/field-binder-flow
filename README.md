# FieldBinder

FieldBinder is a little sibling of Binder for special case of single field bindings. FieldBinder enables to use same Converters, Validators and similar API to Binder with single field binding.

FieldBinder connects one Field component with value with one direction binding.

A binder is a binding, representing the mapping of a single field, through converters and validators, and acts as a buffer for bound value.

A binder instance can be bound to a single value and field instance at a time, but can be rebound as needed.

This add-on does not have client side implementation, frontend resources not included.

Source code of Vaadin 8 version of the add-on is found at [https://github.com/TatuLund/FieldBinder](https://github.com/TatuLund/FieldBinder)

## Development instructions

Starting the test/demo server:
1. Run `mvn jetty:run`.
2. Open http://localhost:8080 in the browser.

## Publishing to Vaadin Directory

You can create the zip package needed for [Vaadin Directory](https://vaadin.com/directory/) using
```
mvn versions:set -DnewVersion=2.0.0 # You cannot publish snapshot versions 
mvn install -Pdirectory
```

The package is created as `target/fieldbinder-2.0.0.zip`

For more information or to upload the package, visit https://vaadin.com/directory/my-components?uploadNewComponent
