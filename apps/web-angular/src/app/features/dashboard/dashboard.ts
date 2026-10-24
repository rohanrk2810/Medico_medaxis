import {Component,inject,signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {AuthService} from '../../core/auth/auth.service';
interface Tenant {legalName:string;timezone:string;currency:string;country:string;}
@Component({standalone:true,imports:[MatButtonModule],template:`<h1>Hospital workspace</h1>@if(tenant();as t){<h2>{{t.legalName}}</h2><p>{{t.country}} · {{t.timezone}} · {{t.currency}}</p>}@else if(!error()){<p role="status">Loading hospital configuration…</p>}@if(error()){<p role="alert">{{error()}}</p>}<button mat-button (click)="renew()">Renew session</button><button mat-button (click)="logout()">Sign out</button><p>Patient and hospital management workflows are under development.</p>`})
export class Dashboard {
 private auth=inject(AuthService);private http=inject(HttpClient);private router=inject(Router);
 readonly tenant=signal<Tenant|null>(null);readonly error=signal('');
 constructor(){this.load();}
 load(){this.error.set('');this.http.get<Tenant>(`/api/v1/tenants/${this.auth.session()?.tenantId}`).subscribe({next:t=>this.tenant.set(t),error:()=>this.error.set('Hospital configuration is unavailable. Your session or permissions may need attention.')});}
 renew(){this.auth.refresh().subscribe({next:()=>this.load(),error:()=>{this.auth.session.set(null);void this.router.navigateByUrl('/login');}});}
 logout(){this.auth.logout().subscribe({next:()=>void this.router.navigateByUrl('/login'),error:()=>this.error.set('Sign-out was not confirmed. Please retry.')});}
}
