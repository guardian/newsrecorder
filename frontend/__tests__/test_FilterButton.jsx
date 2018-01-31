import React from 'react';
import {shallow,mount} from 'enzyme';
import FilterButton from '../FilterButton.jsx';
import sinon from 'sinon';

describe("FilterButton", ()=>{
    test("should call onActivate callback when activated", ()=>{
        const onActivateSpy = sinon.spy();
        const parentSpy = sinon.spy();

        const rendered = shallow(<FilterButton fieldName="myField" values={['myValue']} parent={parentSpy} onActivate={onActivateSpy} type="plus"/>);
        rendered.find('i').simulate("click");

        expect(onActivateSpy.calledOnce).toBeTruthy();
        expect(onActivateSpy.calledWith("myField", ['myValue'])).toBeTruthy();
    });

    test("should call onDeactivate callback when de-activated", ()=>{
        const onActivateSpy = sinon.spy();
        const onDeactivateSpy = sinon.spy();
        const parentSpy = sinon.spy();

        const rendered = shallow(<FilterButton fieldName="myField" values={['myValue']} parent={parentSpy}
                                               onActivate={onActivateSpy} onDeactivate={onDeactivateSpy} type="plus"/>);
        rendered.find('i').simulate("click");   //activate with first click
        rendered.find('i').simulate("click");   //deactivate with second

        expect(onDeactivateSpy.calledOnce).toBeTruthy();
        expect(onDeactivateSpy.calledWith("myField", ['myValue'])).toBeTruthy();
    });

    test("should render font-awesome's fa-search-plus if it's type plus and not active", ()=>{
        const rendered = shallow(<FilterButton fieldName="myField" values={['myvalue']} parent={this} type="plus"/>);
        rendered.instance().setState({active: false});
        rendered.update();

        expect(rendered.find('i').hasClass("fa-search-plus")).toBeTruthy();
        expect(rendered.find('i').hasClass("fa-search-minus")).toBeFalsy();

    });

    test("should render font-awesome's fa-search-minus if it's type minus and not active", ()=>{
        const rendered = shallow(<FilterButton fieldName="myField" values={['myvalue']} parent={this} type="minus"/>);
        rendered.instance().setState({active: false});
        rendered.update();

        expect(rendered.find('i').hasClass("fa-search-plus")).toBeFalsy();
        expect(rendered.find('i').hasClass("fa-search-minus")).toBeTruthy();

    });

    test("should render nothing if it is active", ()=>{
        const rendered = shallow(<FilterButton fieldName="myField" values={['myvalue']} parent={this} type="plus"/>);
        rendered.instance().setState({active: true});
        rendered.update();

        expect(rendered.find('i').hasClass("fa-search-plus")).toBeFalsy();
        expect(rendered.find('i').hasClass("fa-search-minus")).toBeFalsy();
    });

    test("should not render anything if values is blank", ()=>{
        const rendered = shallow(<FilterButton fieldName="myField" values={null} parent={this} type="plus"/>);
        expect(rendered.find('i').hasClass("fa-search-plus")).toBeFalsy();
        expect(rendered.find('i').hasClass("fa-search-minus")).toBeFalsy();
    });

});