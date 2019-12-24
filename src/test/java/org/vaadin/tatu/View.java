package org.vaadin.tatu;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import org.vaadin.fieldbinder.FieldBinder;
import org.vaadin.fieldbinder.FieldBinder.FieldBinding;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.LocalDateToDateConverter;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.validator.DateRangeValidator;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

	String textValue = ""; 
	Integer integerValue = 0;
	Double doubleValue = 0d;
	Date dateValue = new Date();
	
    public class MyDateConverter extends LocalDateToDateConverter {
        @Override
        public Result<Date> convertToModel(LocalDate localDate,
                ValueContext context) {
            if (localDate == null) {
                return Result.ok(Date.from(Instant.ofEpochSecond(0)));
            }

            return super.convertToModel(localDate, context);
        }    	
    }
    
    public View() {
        FieldBinder<String> textFieldBinder = new FieldBinder<>();
        TextField textField = new TextField("Input text");

        // Text field with String value and validator
        // Demoing how to detect if value is valid and how to get it from FieldBinder
        textFieldBinder.forField(textField)
        		.asRequired()
        		.withValidator(new StringLengthValidator("Input needs to be between 5 and 10 characters",5,10))
        		.bind(textValue);
        textFieldBinder.validate();
        textFieldBinder.addValueChangeListener(event ->  {
        	System.out.println("String: "+textValue);
        	System.out.println("String from event: "+event.getValue());
        	System.out.println("Valid Integer from binder: "+textFieldBinder.getValue());
        	System.out.println("Is valid: "+textFieldBinder.isValid());
        });

        // Binder with integer
        FieldBinder<Integer> integerFieldBinder = new FieldBinder<>();
        TextField integerField = new TextField("Input number");

        // Text field with Integer value Converter and Validator
        // Demoing how to detect if value is valid and how to get it from FieldBinding
        FieldBinding<Integer> integerBinding = integerFieldBinder.forField(integerField)
        		.withConverter(new StringToIntegerConverter("This is not a number"))
        		.withValidator(new IntegerRangeValidator("Give a number between 5 and 10",5,10))
        		.bind(integerValue);
        integerFieldBinder.addValueChangeListener(event ->  {
        	System.out.println("Integer: "+integerValue);            
        	System.out.println("String from event via field: "+event.getValue());
        	System.out.println("Valid String from binding: "+integerBinding.getValue());
        	System.out.println("Is valid: "+integerFieldBinder.isValid());
        });

        // Text field with Double value Converter and Validator
        // Demoing how to detect if value is valid, showing customized error using status handler
        TextField doubleField = new TextField("Input double");       
        FieldBinder.of(doubleField)
        		.withConverter(new StringToDoubleConverter("This is not a number"))
        		.withValidator(new DoubleRangeValidator("Give a number between -10 and 10",-10.0d,10.0d))
        		.withValidationStatusHandler(status -> { 
        			status.getMessage().ifPresent(message -> Notification.show(message,2000,Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR));
        			if (!status.isError()) doubleValue = (Double) status.getBinding().getValue();
        			System.out.println("New valid double: "+doubleValue);
        			})
        		.bind(doubleValue);

        // Text field with Date value Validator against Field type and Converter to model type
        // Demoing how to show validation status in custom label
        HorizontalLayout dateLayout = new HorizontalLayout();
        DatePicker dateField = new DatePicker("Select date");
        Span dateLabel = new Span();
        dateLayout.add(dateLabel,dateField);
        dateLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        dateLayout.setAlignItems(Alignment.CENTER);
        
        FieldBinder.of(dateField)
                .withValidator(new DateRangeValidator(
                        "Date cant be from the past", LocalDate.now(),
                        LocalDate.MAX))
                .withConverter(new MyDateConverter())
                .withStatusLabel(dateLabel)
        		.bind(dateValue);

        // Show it in the middle of the screen
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.add(textField,integerField,doubleField,dateLayout);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setAlignItems(Alignment.CENTER);
        add(layout);
    }
}
