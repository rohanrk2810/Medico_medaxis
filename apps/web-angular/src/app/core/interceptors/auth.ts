import {inject} from '@angular/core';
import {HttpInterceptorFn} from '@angular/common/http';
import {AuthService} from '../auth/auth.service';
export const authInterceptor:HttpInterceptorFn=(req,next)=>{
 const session=inject(AuthService).session();
 return next(session && req.url.startsWith('/api/v1/')?req.clone({setHeaders:{Authorization:`Bearer ${session.accessToken}`}}):req);
};
