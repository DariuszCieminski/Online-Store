import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QuantityPanelComponent } from './quantity-panel.component';

describe('QuantityPanelComponent', () => {
    let component: QuantityPanelComponent;
    let fixture: ComponentFixture<QuantityPanelComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [QuantityPanelComponent]
        })
               .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(QuantityPanelComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
