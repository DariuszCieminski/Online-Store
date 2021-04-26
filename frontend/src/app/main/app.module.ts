import { registerLocaleData } from "@angular/common";
import { HttpClientModule } from "@angular/common/http";
import localePl from '@angular/common/locales/pl';
import { NgModule } from '@angular/core';
import { FlexLayoutModule } from "@angular/flex-layout";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatBadgeModule } from "@angular/material/badge";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatDialogModule } from "@angular/material/dialog";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatGridListModule } from "@angular/material/grid-list";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatRadioModule } from "@angular/material/radio";
import { MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatSortModule } from "@angular/material/sort";
import { MatStepperModule } from "@angular/material/stepper";
import { MatTableModule } from "@angular/material/table";
import { MatToolbarModule } from '@angular/material/toolbar';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from "@angular/router";
import { NgxPermissionsGuard, NgxPermissionsModule } from 'ngx-permissions';
import { AuthenticationGuard } from "./services/authentication/authentication-guard";
import { PROVIDERS } from "../../environments/providers";
import { AppComponent } from './app.component';
import { ButtonSpinnerComponent } from './components/button-spinner/button-spinner.component';
import { CartComponent } from './views/cart/cart.component';
import { FilterPanelComponent } from './components/filter-panel/filter-panel.component';
import { HomeComponent } from './views/home/home.component';
import { LoginComponent } from './views/login/login.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { NewOrderComponent } from './views/new-order/new-order.component';
import { OrdersComponent } from './views/orders/orders.component';
import { ProductDataComponent } from './views/products/product-data/product-data.component';
import { ProductDeleteComponent } from './views/products/product-delete.component';
import { ProductDetailsComponent } from './views/products/product-details/product-details.component';
import { ProductsComponent } from './views/products/products.component';
import { QuantityPanelComponent } from './views/products/quantity-panel/quantity-panel.component';
import { RegisterComponent } from './views/register/register.component';
import { CurrencyPLNPipe } from './pipes/currency-pln.pipe';
import { CustomDatePipe } from './pipes/custom-date.pipe';

registerLocaleData(localePl);

@NgModule({
    declarations: [
        AppComponent,
        NavbarComponent,
        LoginComponent,
        RegisterComponent,
        HomeComponent,
        ProductsComponent,
        ProductDetailsComponent,
        CartComponent,
        ProductDataComponent,
        FilterPanelComponent,
        ProductDeleteComponent,
        QuantityPanelComponent,
        NewOrderComponent,
        OrdersComponent,
        CurrencyPLNPipe,
        CustomDatePipe,
        ButtonSpinnerComponent
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        RouterModule.forRoot([
            {
                path: "",
                component: HomeComponent,
                canActivate: [AuthenticationGuard],
                data: {guestAllowed: true}
            },
            {
                path: "login",
                component: LoginComponent,
                canActivate: [NgxPermissionsGuard],
                data: {permissions: {only: 'GUEST', redirectTo: '/'}}
            },
            {
                path: "register",
                component: RegisterComponent,
                canActivate: [NgxPermissionsGuard],
                data: {permissions: {only: 'GUEST', redirectTo: '/'}}
            },
            {
                path: "products",
                component: ProductsComponent,
                canActivate: [NgxPermissionsGuard, AuthenticationGuard],
                data: {permissions: {except: 'GUEST', redirectTo: '/login'}}
            },
            {
                path: "cart",
                component: CartComponent,
                canActivate: [NgxPermissionsGuard, AuthenticationGuard],
                data: {permissions: {except: 'GUEST', redirectTo: '/login'}}
            },
            {
                path: "newOrder",
                component: NewOrderComponent,
                canActivate: [NgxPermissionsGuard, AuthenticationGuard],
                data: {permissions: {except: 'GUEST', redirectTo: '/login'}}
            },
            {
                path: "orders",
                component: OrdersComponent,
                canActivate: [NgxPermissionsGuard, AuthenticationGuard],
                data: {permissions: {except: 'GUEST', redirectTo: '/login'}}
            }
        ]),
        NgxPermissionsModule.forRoot(),
        MatToolbarModule,
        BrowserAnimationsModule,
        FormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule,
        MatSnackBarModule,
        ReactiveFormsModule,
        MatStepperModule,
        MatGridListModule,
        MatCardModule,
        MatPaginatorModule,
        MatDialogModule,
        FlexLayoutModule,
        MatExpansionModule,
        MatBadgeModule,
        MatRadioModule,
        MatTableModule,
        MatSortModule,
        MatProgressSpinnerModule
    ],
    providers: [
        PROVIDERS,
        {
            provide: MAT_SNACK_BAR_DEFAULT_OPTIONS,
            useValue: {
                duration: 3000,
                horizontalPosition: "center",
                verticalPosition: "bottom"
            }
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}