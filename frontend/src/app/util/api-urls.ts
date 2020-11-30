export class ApiUrls {
    private static readonly BASE_URL: string = 'http://localhost:8080';
    private static readonly USERS: string = '/api/users';
    private static readonly PRODUCTS: string = '/api/products';
    private static readonly ORDERS: string = '/api/orders';

    public static get currentUser(): string {
        return this.BASE_URL + this.USERS + '/currentuser';
    }

    public static get login(): string {
        return this.BASE_URL + '/login';
    }

    public static get logout(): string {
        return this.BASE_URL + '/logout';
    }

    public static get swagger(): string {
        return this.BASE_URL + "/swagger-ui/index.html";
    }

    public static get users(): string {
        return this.BASE_URL + this.USERS;
    }

    public static get products(): string {
        return this.BASE_URL + this.PRODUCTS;
    }

    public static get orders(): string {
        return this.BASE_URL + this.ORDERS;
    }

    public static ordersForBuyer(buyerId: number): string {
        return this.BASE_URL + this.ORDERS + `/buyer/${buyerId}`;
    }
}