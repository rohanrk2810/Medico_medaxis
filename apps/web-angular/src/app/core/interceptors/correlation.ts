import {HttpInterceptorFn} from '@angular/common/http';
export const correlationInterceptor:HttpInterceptorFn=(req,next)=>next(req.url.startsWith('/api/v1/')?req.clone({setHeaders:{'X-Correlation-ID':crypto.randomUUID()}}):req);
