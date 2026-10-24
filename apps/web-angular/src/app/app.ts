import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
@Component({selector:'app-root',standalone:true,imports:[RouterOutlet],template:`<header><strong>Medical ERP</strong><span>Workspace setup</span></header><main><router-outlet /></main>`})
export class App {}
