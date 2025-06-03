import { BrowserModule } from "@angular/platform-browser";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";

import { LoginComponent as UserLoginComponent } from "./components/user/login/login.component";
import { CreateComponent as UserCreateComponent } from "./components/user/create/create.component";
import { RouterLink } from "@angular/router";
import { UserComponent } from "./components/user/user.component";

import { AppRoutingModule } from "./app-routing.module";
import { AppComponent } from "./app.component";
import { HeaderComponent } from "./components/header/header.component";
import { FooterComponent } from "./components/footer/footer.component";
import { HomepageComponent } from "./components/homepage/homepage.component";
import { HomeComponent } from "./components/home/home.component";
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";
import { httpInterceptorProviders } from "./interceptors";
import { ResizeService } from "./interaction-services/resize.service";
import { DragService } from "./interaction-services/drag.service";
import { CommonModule } from "@angular/common";
import { ComponentsModule } from "./components/comp/components.module";

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    HomeComponent,
    UserLoginComponent,
    UserCreateComponent,
    UserComponent,
  ],
  bootstrap: [AppComponent],
  imports: [
    ComponentsModule,
    CommonModule,
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    RouterLink,
  ],
  providers: [httpInterceptorProviders, provideHttpClient(withInterceptorsFromDi()), ResizeService, DragService],
})
export class AppModule {}
