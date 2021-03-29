import { Component, Input } from '@angular/core';
import { ThemePalette } from "@angular/material/core";

@Component({
    selector: 'button-spinner',
    templateUrl: './button-spinner.component.html',
    styleUrls: ['./button-spinner.component.css']
})
export class ButtonSpinnerComponent {
    @Input() text: string;
    @Input() spinning: boolean = false;
    @Input() disabled: boolean = false;
    @Input() color: ThemePalette;
    @Input() icon: string;
}