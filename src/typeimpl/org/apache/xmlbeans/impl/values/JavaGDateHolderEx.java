/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2000-2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateSpecification;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;

import java.util.Date;
import java.util.Calendar;

public abstract class JavaGDateHolderEx extends XmlObjectBase
{
    public JavaGDateHolderEx(SchemaType type, boolean complex)
    {
        _schemaType = type;
        initComplexType(complex, false);
    }

    public SchemaType schemaType()
        { return _schemaType; }

    private SchemaType _schemaType;
    private GDate _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // sets/gets raw text value
    protected String compute_text(NamespaceManager nsm)
        { return _value == null ? "" : _value.toString(); }

    protected void set_text(String s)
    {
        GDate newVal;
        if (_validateOnSet())
            newVal = validateLexical(s, _schemaType, _voorVc);
        else
            newVal = lex(s, _schemaType, _voorVc);

        if (_validateOnSet() && newVal != null)
            validateValue(newVal, _schemaType, _voorVc);

        _value = newVal;
    }

    public static GDate lex(String v, SchemaType sType, ValidationContext context)
    {
        GDate date = null;
        
        try
        {
            date = new GDate(v);
        }
        catch (Exception e)
        {
            context.invalid("Date value is malformed: "+v);
        }

        if (date != null)
        {
            if (date.getBuiltinTypeCode() != sType.getPrimitiveType().getBuiltinTypeCode())
            {
                context.invalid("Date value is of wrong type: " + v);
                date = null;
            }
            else if (!date.isValid())
            {
                context.invalid("Date value is invalid: " + v);
                date = null;
            }
        }

        return date;
    }

    public static GDate validateLexical(String v, SchemaType sType, ValidationContext context)
    {

        GDate date = lex(v, sType, context);

        if (date != null && sType.hasPatternFacet())
            if (!sType.matchPatternFacet(v))
                context.invalid("Date (' + v + ') does not match pattern for " + QNameHelper.readable(sType));
        
        return date;
    }

    public static void validateValue(GDateSpecification v, SchemaType sType, ValidationContext context)
    {
        XmlObject x;
        GDate g;
        
        if (v.getBuiltinTypeCode() != sType.getPrimitiveType().getBuiltinTypeCode())
            context.invalid("Date (" + v + ") does not have the set of fields required for " + QNameHelper.readable(sType));

        if ((x = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) <= 0)
                context.invalid("Date (" + v + ") is less than or equal to min exclusive facet (" + g + ") for " + QNameHelper.readable(sType) );
        
        if ((x = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) < 0)
                context.invalid("Date (" + v + ") is less than min inclusive facet (" + g + ") for " + QNameHelper.readable(sType) );
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) >= 0)
                context.invalid("Date (" + v + ") is greater than or equal to max exclusive facet (" + g + ") for " + QNameHelper.readable(sType) );
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) > 0)
                context.invalid("Date (" + v + ") is greater than max inclusive facet (" + g + ") for " + QNameHelper.readable(sType) );
        
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (v.compareToGDate(((XmlObjectBase)vals[i]).gDateValue()) == 0)
                    return;
            context.invalid("Date (" + v + ") is not a valid enumeration value for " + QNameHelper.readable(sType));
        }
    }

    protected void set_nil()
    {
        _value = null;
    }

    // numerics: gYear, gMonth, gDay accept an integer
    public int intValue()
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        if (code != SchemaType.BTC_G_DAY &&
                code != SchemaType.BTC_G_MONTH &&
                code != SchemaType.BTC_G_YEAR)
            throw new XmlValueOutOfRangeException();

        check_dated();

        if (_value == null)
            return 0;

        switch (code)
        {
            case SchemaType.BTC_G_DAY:
                return _value.getDay();
            case SchemaType.BTC_G_MONTH:
                return _value.getMonth();
            case SchemaType.BTC_G_YEAR:
                return _value.getYear();
            default:
                assert(false);
                throw new IllegalStateException();
        }
    }

    public GDate gDateValue()
    {
        check_dated();

        if (_value == null)
            return null;

        return _value;
    }
    
    public Calendar calendarValue()
    {
        check_dated();

        if (_value == null)
            return null;

        return _value.getCalendar();
    }

    public Date dateValue()
    {
        check_dated();

        if (_value == null)
            return null;

        return _value.getDate();
    }

    // setters
    protected void set_int(int v)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        if (code != SchemaType.BTC_G_DAY &&
                code != SchemaType.BTC_G_MONTH &&
                code != SchemaType.BTC_G_YEAR)
            throw new XmlValueOutOfRangeException();

        GDateBuilder value = new GDateBuilder();

        switch (code)
        {
            case SchemaType.BTC_G_DAY:
                value.setDay(v); break;
            case SchemaType.BTC_G_MONTH:
                value.setMonth(v); break;
            case SchemaType.BTC_G_YEAR:
                value.setYear(v); break;
        }

        if (_validateOnSet())
            validateValue(value, _schemaType, _voorVc);

        _value = value.toGDate();
    }

    protected void set_GDate(GDateSpecification v)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        GDate candidate;

        if (v.isImmutable() && (v instanceof GDate) && v.getBuiltinTypeCode() == code)
            candidate = (GDate)v;
        else
        {
            // truncate extra fields from the date if necessary.
            if (v.getBuiltinTypeCode() != code)
            {
                GDateBuilder gDateBuilder = new GDateBuilder(v);
                gDateBuilder.setBuiltinTypeCode(code);
                v = gDateBuilder;
            }
            candidate = new GDate(v);
        }

        if (_validateOnSet())
            validateValue(candidate, _schemaType, _voorVc);

        _value = candidate;
    }
    
    protected void set_Calendar(Calendar c)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        GDateBuilder gDateBuilder = new GDateBuilder(c);
        gDateBuilder.setBuiltinTypeCode(code);
        GDate value = gDateBuilder.toGDate();
 
        if (_validateOnSet())
            validateValue(value, _schemaType, _voorVc);

        _value = value;
    }

    protected void set_Date(Date v)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        if (code != SchemaType.BTC_DATE && code != SchemaType.BTC_DATE_TIME ||
            v == null)
            throw new XmlValueOutOfRangeException();

        GDateBuilder gDateBuilder = new GDateBuilder(v);
        gDateBuilder.setBuiltinTypeCode(code);
        GDate value = gDateBuilder.toGDate();
 
        if (_validateOnSet())
            validateValue(value, _schemaType, _voorVc);

        _value = value;
    }


    // comparators
    protected int compare_to(XmlObject obj)
    {
        return _value.compareToGDate(((XmlObjectBase)obj).gDateValue());
    }

    protected boolean equal_to(XmlObject obj)
    {
        return _value.equals(((XmlObjectBase)obj).gDateValue());
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
    
    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(gDateValue(), schemaType(), ctx);
    }
    
}