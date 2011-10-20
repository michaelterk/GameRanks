package michael.ranks.webinject;

import javax.ws.rs.FormParam;

import michael.ranks.webinject.Validate.Rules;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.sun.jersey.api.ParamException;

/**
 * make sure this runs first on the food chain before jersey tries it's conversions
 * 
 * @author Michael
 *
 */
public class ValidationInterceptor implements MethodInterceptor {
  public Object invoke(MethodInvocation invocation) throws Throwable {
	Validate a =  invocation.getMethod().getAnnotation(Validate.class);
	
	for(Rules rule : a.value()) {
		if(rule.equals(Rules.NOT_EMPTY)) {
			int i=0;
			for(Object obj: invocation.getArguments()) {
				if(obj==null||((obj instanceof String)&&((String)obj).isEmpty())) {
					String paramName = ((FormParam)invocation.getMethod().getParameterAnnotations()[i][0]).value();
					throw new ParamException.FormParamException(null,"All paramters must have value ("+paramName+")",null);
				}
				i++;
			}
		}		
	}
	
    return invocation.proceed();
  }
}
