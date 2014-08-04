package org.cirqwizard.settings;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by simon on 04/08/14.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PreferenceGroup
{
    String name();
}
