import {inject, Injectable, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs';
interface Session {accessToken:string;refreshToken:string;expiresIn:number;tenantId:string;branchId:string;}
@Injectable({providedIn:'root'})
export class AuthService {
 private http=inject(HttpClient);
 readonly session=signal<Session|null>(null);
 login(value:{tenantId:string;branchId:string;email:string;password:string}) {return this.http.post<Session>('/api/v1/auth/login',value).pipe(tap(s=>this.session.set(s)));}
 refresh(){const s=this.session();if(!s)throw new Error('Session required');return this.http.post<Session>('/api/v1/auth/refresh',{refreshToken:s.refreshToken,branchId:s.branchId}).pipe(tap(next=>this.session.set(next)));}
 logout(){const s=this.session();return this.http.post<void>('/api/v1/auth/logout',{refreshToken:s?.refreshToken}).pipe(tap(()=>this.session.set(null)));}
}
