import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "./guards/auth.guard";

import { HomeComponent } from "./components/home/home.component";

import { LoginComponent } from "./components/user/login/login.component";
import { CreateComponent } from "./components/user/create/create.component";
import { UserComponent } from "./components/user/user.component";

import { NotFoundComponent } from "./components/not-found/not-found.component";

const routes: Routes = [
  { path: "", component: HomeComponent, canActivate: [AuthGuard] },
  { path: "component/:id", component: HomeComponent, canActivate: [AuthGuard] },
  { path: "login", component: LoginComponent },
  { path: "signup", component: CreateComponent },
  { path: "user/:name", component: UserComponent, canActivate: [AuthGuard] },
  { path: "**", component: NotFoundComponent, canActivate: [AuthGuard] },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
