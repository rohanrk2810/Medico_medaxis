import {Component,inject,signal} from '@angular/core';
import {FormBuilder,ReactiveFormsModule,Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {AuthService} from '../../core/auth/auth.service';
@Component({standalone:true,imports:[ReactiveFormsModule,MatFormFieldModule,MatInputModule,MatButtonModule],template:`<h1>Sign in to your hospital</h1><p>Use the hospital and branch identifiers supplied by your administrator.</p><form [formGroup]="form" (ngSubmit)="submit()">@for(field of fields;track field.key){<mat-form-field><mat-label>{{field.label}}</mat-label><input matInput [formControlName]="field.key" [type]="field.type" [autocomplete]="field.autocomplete">@if(form.controls[field.key].invalid){<mat-error>Enter a valid {{field.label.toLowerCase()}}</mat-error>}</mat-form-field>}<button mat-flat-button [disabled]="busy() || form.invalid">{{busy()?'Signing in…':'Sign in'}}</button>@if(error()){<p role="alert">{{error()}}</p>}</form>`,styles:[`form{max-width:440px;display:grid;gap:14px}`]})
export class Login {
 private auth=inject(AuthService);private router=inject(Router);private fb=inject(FormBuilder);
 readonly busy=signal(false);readonly error=signal('');
 readonly fields=[{key:'tenantId',label:'Hospital ID',type:'text',autocomplete:'off'},{key:'branchId',label:'Branch ID',type:'text',autocomplete:'off'},{key:'email',label:'Email',type:'email',autocomplete:'username'},{key:'password',label:'Password',type:'password',autocomplete:'current-password'}] as const;
 private uuid=[Validators.required,Validators.pattern(/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i)];
 readonly form=this.fb.nonNullable.group({tenantId:['',this.uuid],branchId:['',this.uuid],email:['',[Validators.required,Validators.email,Validators.maxLength(254)]],password:['',[Validators.required,Validators.maxLength(72)]]});
 submit(){if(this.form.invalid||this.busy())return;this.busy.set(true);this.error.set('');this.auth.login(this.form.getRawValue()).subscribe({next:()=>{this.form.controls.password.reset();this.busy.set(false);void this.router.navigateByUrl('/');},error:()=>{this.busy.set(false);this.error.set('Sign-in failed. Check your details or try again later.');}});}
}
