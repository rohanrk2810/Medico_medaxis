import {ApplicationConfig} from '@angular/core';
import {provideRouter} from '@angular/router';
import {provideHttpClient,withInterceptors} from '@angular/common/http';
import {authInterceptor} from './core/interceptors/auth';
import {routes} from './app.routes';
import {correlationInterceptor} from './core/interceptors/correlation';
export const appConfig:ApplicationConfig={providers:[provideRouter(routes),provideHttpClient(withInterceptors([correlationInterceptor,authInterceptor]))]};
